package com.jppappstudio.mediafeedplayer.android.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChannelViewModel(application: Application): AndroidViewModel(application) {

    private val channelDao: ChannelDao
    var allChannels: LiveData<List<Channel>>

    init {
        channelDao = AppRoomDatabase.getDatabase(application, viewModelScope).ChannelDao()
        allChannels = channelDao.getAll()
    }

    fun insert(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        channelDao.addChannel(channel)
    }

    fun delete(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        channelDao.deleteChannel(channel)
    }

    fun update(channel: Channel) = viewModelScope.launch(Dispatchers.IO) {
        channelDao.updateChannel(channel)
    }

    fun updateById(id: Int, name: String, url: String) = viewModelScope.launch(Dispatchers.IO) {
        channelDao.updateChannelById(id, name, url)
    }
}