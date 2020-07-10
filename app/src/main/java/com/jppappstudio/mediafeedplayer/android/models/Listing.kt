package com.jppappstudio.mediafeedplayer.android.models

import java.util.regex.Pattern

class Listing(
    val title: String,
    val url: String,
    var thumbnailURL: String = "",
    val description: String = "",
    val type: String = "",
    val bookmarkable: Boolean = true
) {
    init {
        if (thumbnailURL == "") {
            thumbnailURL = extractImgSrcFromString(description)
        }
    }

    // https://stackoverflow.com/questions/25545370/extract-image-src-from-imgtag-in-android
    fun extractImgSrcFromString(string: String): String {

        val imgRegex = "<[iI][mM][gG][^>]+[sS][rR][cC]\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>"
        val p = Pattern.compile(imgRegex)
        val m = p.matcher(string)

        if (m.find()) {
            return m.group(1)
        }

        return ""
    }
}