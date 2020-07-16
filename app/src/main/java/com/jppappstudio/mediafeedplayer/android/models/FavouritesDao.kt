package com.jppappstudio.mediafeedplayer.android.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourites ORDER BY id ASC")
    fun getAll(): LiveData<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavourite(listing: Listing)

    @Delete
    fun deleteFavourite(listing: Listing)
}