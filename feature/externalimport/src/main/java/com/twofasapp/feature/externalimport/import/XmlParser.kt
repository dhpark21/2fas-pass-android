package com.twofasapp.feature.externalimport.import

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object XmlParser {
    fun parse(
        inputStream: InputStream,
        config: XmlPullParser.() -> Unit = {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        },
        block: XmlPullParser.(ParsingContext) -> Unit
    ) {
        val parser = Xml.newPullParser()
        inputStream.use { input ->
            parser.setInput(input, null)
            config(parser)

            while (parser.eventType != XmlPullParser.START_TAG && parser.eventType != XmlPullParser.END_DOCUMENT) {
                parser.next()
            }
            parser.require(XmlPullParser.START_TAG, null, null)

            val parsingContext = ParsingContext()
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        parsingContext.addTag(parser.nameOrEmpty())
                    }
                }
                block(parser, parsingContext)
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

class ParsingContext {

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

    fun tagFromLast(offset: Int): String? {
        if (offset < 0) return null
        val index = currentTags.lastIndex - offset
        return currentTags.getOrNull(index)
    }
}

fun XmlPullParser.nameOrEmpty(): String {
    return name ?: ""
}

fun XmlPullParser.textOrEmpty(): String {
    return text ?: ""
}

fun XmlPullParser.parseWithContext(
    predicate: XmlPullParser.(ParsingContext) -> Boolean,
    block: XmlPullParser.(ParsingContext) -> Unit
) {
    val parsingContext = ParsingContext()
    while (predicate(this, parsingContext)) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                parsingContext.addTag(nameOrEmpty())
            }
        }
        block(this, parsingContext)
        when (eventType) {
            XmlPullParser.END_TAG -> {
                parsingContext.removeTag()
            }
        }
        if (predicate(this, parsingContext)) {
            next()
        } else {
            break
        }
    }
}