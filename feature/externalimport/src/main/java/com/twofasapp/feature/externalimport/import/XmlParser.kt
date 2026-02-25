package com.twofasapp.feature.externalimport.import

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object XmlParser {
    fun parse(
        inputStream: InputStream,
        block: (ParsingContext) -> Unit,
    ) {
        val parser = Xml.newPullParser()
        inputStream.use { input ->
            parser.setInput(input, null)

            while (parser.eventType != XmlPullParser.START_TAG && parser.eventType != XmlPullParser.END_DOCUMENT) {
                parser.next()
            }
            parser.require(XmlPullParser.START_TAG, null, null)

            val parsingContext = ParsingContext(parser)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        parsingContext.addTag(parser.name ?: "")
                    }
                }
                block(parsingContext)
                when (parser.eventType) {
                    XmlPullParser.END_TAG -> {
                        parsingContext.removeTag()
                    }
                }
                parser.next()
            }
        }
    }
}

class ParsingContext(private val parser: XmlPullParser) {

    private val currentTags = mutableListOf<String>()

    fun addTag(name: String) {
        currentTags.add(name)
    }

    fun removeTag() {
        currentTags.removeLastOrNull()
    }

    fun currentTag(): String? {
        return currentTags.lastOrNull()
    }

    fun currentTagOrEmpty(): String {
        return currentTags.lastOrNull() ?: ""
    }

    fun text(): String? {
        return parser.text
    }

    fun textOrEmpty(): String {
        return text() ?: ""
    }

    fun tagFromLast(offset: Int): String? {
        if (offset < 0) return null
        val index = currentTags.lastIndex - offset
        return currentTags.getOrNull(index)
    }

    fun tagFromLastOrEmpty(offset: Int): String {
        return tagFromLast(offset) ?: ""
    }

    fun eventType(): XmlParserEventType? {
        return when (parser.eventType) {
            XmlPullParser.START_DOCUMENT -> XmlParserEventType.StartDocument
            XmlPullParser.END_DOCUMENT -> XmlParserEventType.EndDocument
            XmlPullParser.START_TAG -> XmlParserEventType.StartTag
            XmlPullParser.END_TAG -> XmlParserEventType.EndTag
            XmlPullParser.TEXT -> XmlParserEventType.Text
            XmlPullParser.CDSECT -> XmlParserEventType.Cdsect
            XmlPullParser.ENTITY_REF -> XmlParserEventType.EntityRef
            XmlPullParser.IGNORABLE_WHITESPACE -> XmlParserEventType.IgnorableWhitespace
            XmlPullParser.PROCESSING_INSTRUCTION -> XmlParserEventType.ProcessingInstruction
            XmlPullParser.COMMENT -> XmlParserEventType.Comment
            XmlPullParser.DOCDECL -> XmlParserEventType.Docdecl
            else -> null
        }
    }

    fun withTagScope(
        tag: String,
        block: (ParsingContext) -> Unit,
    ) {
        val parsingContext = ParsingContext(parser)
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    parsingContext.addTag(parser.name ?: "")
                }
            }
            block(parsingContext)
            when (parser.eventType) {
                XmlPullParser.END_TAG -> {
                    parsingContext.removeTag()
                    if (parser.name == tag) {
                        break
                    }
                }
            }
            parser.next()
        }
    }
}

enum class XmlParserEventType {
    StartDocument,
    EndDocument,
    StartTag,
    EndTag,
    Text,
    Cdsect,
    EntityRef,
    IgnorableWhitespace,
    ProcessingInstruction,
    Comment,
    Docdecl,
}