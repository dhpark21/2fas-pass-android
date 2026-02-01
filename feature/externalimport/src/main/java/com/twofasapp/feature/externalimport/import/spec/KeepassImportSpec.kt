/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField.ClearText
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.inputStream
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.XmlParser
import com.twofasapp.feature.externalimport.import.parseWithContext
import com.twofasapp.feature.externalimport.import.textOrEmpty
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber

internal class KeepassImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {

    companion object {
        private const val XML_TAG_STRING = "String"
        private const val XML_TAG_PASSWORD = "Password"
        private const val XML_TAG_TITLE = "Title"
        private const val XML_TAG_URL = "URL"
        private const val XML_TAG_USER_NAME = "UserName"
        private const val XML_TAG_ENTRY = "Entry"
        private const val XML_TAG_HISTORY = "History"
        private const val XML_TAG_KEY = "Key"
        private const val XML_TAG_VALUE = "Value"
        private const val XML_TAG_NAME = "Name"
        private const val XML_TAG_GROUP = "Group"

        private const val CSV_ROW_ACCOUNT = "Account"
        private const val CSV_ROW_LOGIN_NAME = "Login Name"
        private const val CSV_ROW_PASSWORD = "Password"
        private const val CSV_ROW_WEB_SITE = "Web Site"
        private const val CSV_ROW_COMMENTS = "Comments"
    }

    override val type = ImportType.KeePass
    override val name = ImportType.KeePass.displayName
    override val image = com.twofasapp.core.design.R.drawable.external_logo_keepass
    override val instructions = context.getString(R.string.transfer_instructions_keepass)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_keepass),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        return try {
            parseXml(uri, vaultId)
        } catch (t: Throwable) {
            Timber.e(t)
            parseCsv(uri, vaultId)
        }
    }

    private fun parseCsv(uri: Uri, vaultId: String): ImportContent {
        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->
                add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get(CSV_ROW_ACCOUNT),
                            username = row.get(CSV_ROW_LOGIN_NAME),
                            password = row.get(CSV_ROW_PASSWORD),
                            url = row.get(CSV_ROW_WEB_SITE),
                            notes = row.get(CSV_ROW_COMMENTS),
                        ),
                    ),
                )
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }

    private fun parseXml(uri: Uri, vaultId: String): ImportContent {
        val inputStream = uri.inputStream(context)
        requireNotNull(inputStream)

        val tags = mutableListOf<Tag>()
        val items = mutableListOf<Item>()

        val currentTags = mutableListOf<String>()
        val notes = mutableListOf<Pair<String, String>>()
        var loginContent = prepareLoginContent()

        XmlParser.parse(inputStream) { parsingContext ->
            when (eventType) {
                XmlPullParser.START_TAG -> when {
                    parsingContext.currentTag() == XML_TAG_STRING && parsingContext.tagFromLast(1) == XML_TAG_ENTRY && parsingContext.tagFromLast(
                        2
                    ) != XML_TAG_HISTORY -> {
                        var key: String? = null
                        var value: String? = null
                        var parsed = false
                        parseWithContext({ parsed.not() }) { entryParsingContext ->
                            when (eventType) {
                                XmlPullParser.END_TAG -> {
                                    parsed = entryParsingContext.currentTag() == XML_TAG_STRING
                                }

                                XmlPullParser.TEXT -> {
                                    when {
                                        entryParsingContext.currentTag() == XML_TAG_KEY && entryParsingContext.tagFromLast(
                                            1
                                        ) == XML_TAG_STRING -> key = textOrEmpty()

                                        entryParsingContext.currentTag() == XML_TAG_VALUE && entryParsingContext.tagFromLast(
                                            1
                                        ) == XML_TAG_STRING -> value = textOrEmpty()
                                    }
                                }
                            }
                        }
                        loginContent = modifyLoginContent(
                            key = key,
                            value = value,
                            loginContent = loginContent,
                            notesBlock = { notes.add(it) }
                        )
                    }
                }

                XmlPullParser.END_TAG -> when {
                    parsingContext.currentTag() == XML_TAG_GROUP -> currentTags.removeLastOrNull()
                    parsingContext.currentTag() == XML_TAG_ENTRY && parsingContext.tagFromLast(1) != XML_TAG_HISTORY -> {
                        items.add(createLoginItem(vaultId, tags, currentTags, loginContent, notes))
                        loginContent = prepareLoginContent()
                        notes.clear()
                    }
                }

                XmlPullParser.TEXT -> when {
                    parsingContext.currentTag() == XML_TAG_NAME && parsingContext.tagFromLast(1) == XML_TAG_GROUP -> {
                        tags.add(createTag(vaultId, textOrEmpty()))
                        currentTags.add(textOrEmpty())
                    }

                    parsingContext.tagFromLast(1) == XML_TAG_ENTRY && parsingContext.tagFromLast(2) != XML_TAG_HISTORY -> {
                        notes.add(parsingContext.currentTagOrEmpty() to textOrEmpty())
                    }
                }
            }
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = 0,
        )
    }

    private fun prepareLoginContent(): ItemContent.Login {
        return ItemContent.Login.Empty
    }

    private fun createLoginItem(
        vaultId: String,
        tags: List<Tag>,
        itemTags: List<String>,
        content: ItemContent.Login,
        notes: List<Pair<String, String>>
    ): Item {
        return Item.create(
            vaultId = vaultId,
            contentType = ItemContentType.Login,
            content = content.copy(
                iconUriIndex = if (content.uris.isEmpty()) null else 0,
                notes = notes
                    .filter { (key, value) -> key.isNotBlank() && value.isNotBlank() }
                    .joinToString(System.lineSeparator()) { (key, value) -> "${key.trim()}: ${value.trim()}" }),
            tagIds = tags.filter { tag -> itemTags.contains(tag.name) }.map { it.id }
        )
    }

    private fun modifyLoginContent(
        key: String?,
        value: String?,
        loginContent: ItemContent.Login,
        notesBlock: (Pair<String, String>) -> Unit
    ): ItemContent.Login {
        if (value.isNullOrBlank()) {
            return loginContent
        }
        if (key.isNullOrBlank()) {
            return loginContent
        }

        return when (key) {
            XML_TAG_PASSWORD -> loginContent.copy(
                password = ClearText(
                    value.trim()
                )
            )

            XML_TAG_TITLE -> loginContent.copy(name = value.trim())
            XML_TAG_URL -> loginContent.copy(uris = listOf(ItemUri(text = value.trim())))
            XML_TAG_USER_NAME -> loginContent.copy(username = value.trim())
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
            name = name
        )
    }
}