package com.jppappstudio.mediafeedplayer.android.ui.listings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jppappstudio.mediafeedplayer.android.models.Listing
import okhttp3.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.security.MessageDigest
import java.util.*

class ListingsViewModel: ViewModel() {

    var listings = listOf<Listing>()

    init {}
}