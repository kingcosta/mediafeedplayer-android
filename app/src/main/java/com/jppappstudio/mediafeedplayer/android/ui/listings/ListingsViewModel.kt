package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Listing

class ListingsViewModel: ViewModel() {

    var listings = listOf<Listing>()
    var listingTitle = ""

    init {}
}