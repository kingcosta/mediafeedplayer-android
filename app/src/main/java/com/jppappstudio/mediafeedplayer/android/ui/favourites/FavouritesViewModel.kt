package com.jppappstudio.mediafeedplayer.android.ui.favourites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jppappstudio.mediafeedplayer.android.models.AppRoomDatabase
import com.jppappstudio.mediafeedplayer.android.models.FavouritesDao
import com.jppappstudio.mediafeedplayer.android.models.Listing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesViewModel(application: Application): AndroidViewModel(application) {

    private val favouriteDao: FavouritesDao
    var allFavourites: LiveData<List<Listing>>
    var favourited = mutableListOf<Boolean>()

    init {
        favouriteDao = AppRoomDatabase.getDatabase(
            application,
            viewModelScope
        ).FavouritesDao()
        allFavourites = favouriteDao.getAll()
    }

    fun insert(listing: Listing) = viewModelScope.launch(Dispatchers.IO) {
        favouriteDao.addFavourite(listing)
    }

    fun delete(listing: Listing) = viewModelScope.launch(Dispatchers.IO) {
        favouriteDao.deleteFavourite(listing)
    }
}