package com.jppappstudio.mediafeedplayer.android.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.regex.Pattern

@Entity(tableName = "favourites")
class Listing(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "thumbnailURL") var thumbnailURL: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "bookmarkable") val bookmarkable: Boolean = true
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