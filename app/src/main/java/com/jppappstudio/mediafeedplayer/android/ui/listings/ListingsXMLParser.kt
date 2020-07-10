package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.util.Xml
import com.jppappstudio.mediafeedplayer.android.models.Listing
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException

class ListingsXMLParser {

    val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Listing> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readRSS(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readRSS(parser: XmlPullParser): List<Listing> {
        val listings = mutableListOf<Listing>()

        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "channel") {
                return readChannel(parser)
            } else {
                skip(parser)
            }
        }

        return listings
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readChannel(parser: XmlPullParser): List<Listing> {
        val listings = mutableListOf<Listing>()

        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "item") {
                listings.add(readItem(parser))
            } else {
                skip(parser)
            }
        }

        return listings
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readItem(parser: XmlPullParser): Listing {

        var title = ""
        var enclosure = mapOf<String, String>()
        var description = ""
        var thumbnailURL = ""
        var bookmarkable = true
        var url = ""
        var type = ""

        parser.require(XmlPullParser.START_TAG, ns, "item")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "title" -> title = readTitle(parser)
                "enclosure" -> enclosure = readEnclosure(parser)
                "description" -> description = readDescription(parser)
                "media:thumbnail" -> thumbnailURL = readMediaThumbnail(parser)
                else -> skip(parser)
            }
        }

        if (enclosure["bookmarkable"] == "false") {
            bookmarkable = false
        }

        if (enclosure["url"] != null) {
            url = enclosure["url"]!!
        }

        if (enclosure["type"] != null) {
            type = enclosure["type"]!!
        }

        return Listing(
            title,
            url,
            thumbnailURL = thumbnailURL,
            description = description,
            type = type,
            bookmarkable = bookmarkable
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readEnclosure(parser: XmlPullParser): Map<String, String> {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure")

        val url = parser.getAttributeValue(null, "url")
        val type = parser.getAttributeValue(null, "type")
        val bookmarkable = parser.getAttributeValue(null, "bookmark")

        val hash = mapOf(
            "url" to url,
            "type" to type,
            "bookmarkable" to bookmarkable
        )
        parser.nextTag()

        parser.require(XmlPullParser.END_TAG, ns, "enclosure")

        return hash
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readMediaThumbnail(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "media:thumbnail")

        val url = parser.getAttributeValue(null, "url")
        parser.nextTag()

        parser.require(XmlPullParser.END_TAG, ns, "media:thumbnail")

        return url
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "description")

        val description = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")

        return description
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}