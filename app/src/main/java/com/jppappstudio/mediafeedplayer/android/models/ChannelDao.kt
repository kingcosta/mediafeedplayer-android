package com.jppappstudio.mediafeedplayer.android.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAll(): LiveData<List<Channel>>

    @Query("SELECT * FROM channels WHERE id = :id")
    fun findById(id: Int): Channel

    @Query("SELECT * FROM channels WHERE url = :url")
    fun findByUrl(url: String): Channel

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addChannel(channel: Channel)

    @Delete
    fun deleteChannel(channel: Channel)

    @Update
    fun updateChannel(channel: Channel)

    @Query("UPDATE channels SET name = :name, url = :url WHERE id= :id")
    fun updateChannelById(id: Int, name: String, url: String)
}