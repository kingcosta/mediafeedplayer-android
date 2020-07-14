package com.jppappstudio.mediafeedplayer.android.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourites ORDER BY title ASC")
    fun getAll(): LiveData<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addFavourite(listing: Listing)

    @Delete
    fun deleteFavourite(listing: Listing)
}