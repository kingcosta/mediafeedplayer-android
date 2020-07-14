package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jppappstudio.mediafeedplayer.android.models.Listing

class ListingsViewModel(application: Application): AndroidViewModel(application) {

    var listings = listOf<Listing>()
    var listingTitle = ""

    init {}
}