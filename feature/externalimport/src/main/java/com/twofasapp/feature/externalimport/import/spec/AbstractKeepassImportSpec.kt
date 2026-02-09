package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField.ClearText
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.inputStream
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.XmlParser
import com.twofasapp.feature.externalimport.import.XmlParserEventType
import timber.log.Timber

internal abstract class AbstractKeepassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {

    companion object {
        private const val XML_TAG_STRING = "String"
        private const val XML_TAG_ENTRY = "Entry"
        private const val XML_TAG_HISTORY = "History"
        private const val XML_TAG_KEY = "Key"
        private const val XML_TAG_VALUE = "Value"
        private const val XML_TAG_NAME = "Name"
        private const val XML_TAG_GROUP = "Group"
        private const val XML_TAG_TAGS = "Tags"
        private const val XML_TAG_TIMES = "Times"
        private const val XML_TAG_CREATION_TIME = "CreationTime"
        private const val XML_TAG_LAST_MODIFICATION_TIME = "LastModificationTime"

        private const val XML_STRING_KEY_URL = "URL"
        private const val XML_STRING_KEY_PASSWORD = "Password"
        private const val XML_STRING_KEY_USER_NAME = "UserName"
        private const val XML_STRING_KEY_TITLE = "Title"
        private const val XML_STRING_KEY_NOTES = "Notes"
    }

    protected abstract val xmlTagSeparator: String

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        return try {
            parseXml(uri, vaultId)
        } catch (t: Throwable) {
            Timber.e(t)
            parseCsv(uri, vaultId)
        }
    }

    protected abstract fun parseCsv(uri: Uri, vaultId: String): ImportContent
    protected abstract fun parseXmlDate(date: String): Long?

    private fun parseXml(uri: Uri, vaultId: String): ImportContent {
        val inputStream = uri.inputStream(context)
        requireNotNull(inputStream)

        val items = mutableListOf<ParsedItem>()

        val currentTags = mutableListOf<String>()
        val contentTags = mutableListOf<String>()
        val notes = mutableListOf<Pair<String, String>>()
        var loginContent = prepareLoginContent()
        var contentCreationTime = ""
        var contentLastModificationTime = ""

        XmlParser.parse(inputStream) { parsingContext ->
            when (parsingContext.eventType()) {
                XmlParserEventType.StartTag -> when {
                    parsingContext.currentTag() == XML_TAG_STRING &&
                        parsingContext.tagFromLast(1) == XML_TAG_ENTRY &&
                        parsingContext.tagFromLast(2) != XML_TAG_HISTORY -> {
                        var key: String? = null
                        var value: String? = null
                        parsingContext.withTagScope(XML_TAG_STRING) { entryParsingContext ->
                            when (entryParsingContext.eventType()) {
                                XmlParserEventType.Text -> when {
                                    entryParsingContext.currentTag() == XML_TAG_KEY &&
                                        entryParsingContext.tagFromLast(1) == XML_TAG_STRING ->
                                        key = entryParsingContext.textOrEmpty()

                                    entryParsingContext.currentTag() == XML_TAG_VALUE &&
                                        entryParsingContext.tagFromLast(1) == XML_TAG_STRING ->
                                        value = entryParsingContext.textOrEmpty()
                                }

                                else -> {}
                            }
                        }
                        loginContent = modifyLoginContent(
                            key = key,
                            value = value,
                            loginContent = loginContent,
                            notesBlock = { notes.add(it) },
                        )
                    }
                }

                XmlParserEventType.EndTag -> when {
                    parsingContext.currentTag() == XML_TAG_GROUP -> currentTags.removeLastOrNull()
                    parsingContext.currentTag() == XML_TAG_ENTRY &&
                        parsingContext.tagFromLast(1) != XML_TAG_HISTORY -> {
                        items.add(
                            createLoginItem(
                                vaultId = vaultId,
                                itemTags = currentTags + contentTags,
                                content = loginContent,
                                notes = notes,
                                creationTime = contentCreationTime,
                                lastModificationTime = contentLastModificationTime,
                            ),
                        )
                        loginContent = prepareLoginContent()
                        notes.clear()
                        contentTags.clear()
                        contentCreationTime = ""
                        contentLastModificationTime = ""
                    }
                }

                XmlParserEventType.Text -> when {
                    parsingContext.currentTag() == XML_TAG_NAME &&
                        parsingContext.tagFromLast(1) == XML_TAG_GROUP -> {
                        currentTags.add(parsingContext.textOrEmpty())
                    }

                    parsingContext.currentTag() == XML_TAG_TAGS &&
                        parsingContext.tagFromLast(1) == XML_TAG_ENTRY &&
                        parsingContext.tagFromLast(2) != XML_TAG_HISTORY -> {
                        val tagNames = parsingContext.textOrEmpty().split(xmlTagSeparator)
                        contentTags.addAll(tagNames)
                    }

                    parsingContext.currentTag() == XML_TAG_LAST_MODIFICATION_TIME &&
                        parsingContext.tagFromLast(1) == XML_TAG_TIMES &&
                        parsingContext.tagFromLast(2) == XML_TAG_ENTRY &&
                        parsingContext.tagFromLast(3) != XML_TAG_HISTORY ->
                        contentLastModificationTime = parsingContext.textOrEmpty()

                    parsingContext.currentTag() == XML_TAG_CREATION_TIME &&
                        parsingContext.tagFromLast(1) == XML_TAG_TIMES &&
                        parsingContext.tagFromLast(2) == XML_TAG_ENTRY &&
                        parsingContext.tagFromLast(3) != XML_TAG_HISTORY ->
                        contentCreationTime = parsingContext.textOrEmpty()
                }

                else -> {}
            }
        }

        tags.addAll(
            items
                .flatMap { item -> item.tagNames }
                .toSet()
                .map { tagName -> createTag(vaultId, tagName) },
        )

        return ImportContent(
            items = items.map { item -> item.resolve(tags) },
            tags = tags,
            unknownItems = 0,
        )
    }

    private fun prepareLoginContent(): ItemContent.Login {
        return ItemContent.Login.Empty
    }

    private fun createLoginItem(
        vaultId: String,
        itemTags: List<String>,
        content: ItemContent.Login,
        notes: List<Pair<String, String>>,
        creationTime: String,
        lastModificationTime: String,
    ): ParsedItem {
        val item = Item.create(
            vaultId = vaultId,
            contentType = ItemContentType.Login,
            content = content.copy(
                iconUriIndex = if (content.uris.isEmpty()) null else 0,
                notes = createNotes(notes),
            ),
            createdAt = parseXmlDate(creationTime),
            updatedAt = parseXmlDate(lastModificationTime),
        )

        return ParsedItem(
            item = item,
            tagNames = itemTags,
        )
    }

    private fun modifyLoginContent(
        key: String?,
        value: String?,
        loginContent: ItemContent.Login,
        notesBlock: (Pair<String, String>) -> Unit,
    ): ItemContent.Login {
        if (value.isNullOrBlank()) {
            return loginContent
        }
        if (key.isNullOrBlank()) {
            return loginContent
        }

        return when (key) {
            XML_STRING_KEY_PASSWORD -> loginContent.copy(
                password = ClearText(
                    value.trim(),
                ),
            )

            XML_STRING_KEY_TITLE -> loginContent.copy(name = value.trim())
            XML_STRING_KEY_URL -> loginContent.copy(uris = listOf(ItemUri(text = value.trim())))
            XML_STRING_KEY_USER_NAME -> loginContent.copy(username = value.trim())
            else -> {
                notesBlock(key to value)
                loginContent
            }
        }
    }

    private fun createTag(vaultId: String, name: String): Tag {
        return Tag.create(
            vaultId = vaultId,
            id = Uuid.generate(),
            name = name,
        )
    }

    private fun createNotes(notes: List<Pair<String, String>>): String {
        val clearedNotes = notes
            .filter { (key, value) -> key.isNotBlank() && value.isNotBlank() }
            .map { (key, value) -> key.trim() to value.trim() }

        val mainNotes =
            clearedNotes.firstOrNull { (key, _) -> key == XML_STRING_KEY_NOTES }?.second
        val additionalNotes = clearedNotes.filter { (key, _) -> key != XML_STRING_KEY_NOTES }
            .map { (key, value) -> "$key: $value" }

        return buildList {
            mainNotes?.let {
                add(it)
            }
            if (mainNotes != null && additionalNotes.isNotEmpty()) {
                add("")
            }
            addAll(
                clearedNotes.filter { (key, _) -> key != XML_STRING_KEY_NOTES }
                    .map { (key, value) -> "$key: $value" },
            )
        }.joinToString(System.lineSeparator())
    }
}