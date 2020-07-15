package com.jppappstudio.mediafeedplayer.android.ui.channels

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Channel
import kotlinx.android.synthetic.main.add_new_channel.*

class NewChannelActivity: AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var mode = "new"
    var editChannelId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.add_new_channel)
        editText_channel_name.requestFocus()
        firebaseAnalytics = Firebase.analytics

        val bundle: Bundle? = intent.extras
        var newMode: String? = bundle?.getString("mode")

        println("Deeplink: $newMode")

        if (newMode != null) {
            mode = newMode
        }

        if (mode == "edit") {
            supportActionBar?.title = getString(R.string.edit_channel_actionbar_title)

            editChannelId = bundle?.getInt("id")!!

            textView_channal_name.text = getString(R.string.edit_channel_channel_name)
            textView_channel_url.text = getString(R.string.edit_channel_channel_url)

            editText_channel_name.setText(bundle.getString("name"))
            editText_channel_url.setText(bundle.getString("url"))
        }

        if (mode == "new_direct") {
            editText_channel_name.setText(bundle?.getString("name"))
            editText_channel_url.setText(bundle?.getString("url"))
        }

        firebaseAnalytics.logEvent("new_channel_form_open") {}
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_channel_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.new_channel_save -> {
            saveButtonTapped()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun saveButtonTapped() {
        var message = ""
        var readyToSave = true
        var channelName = editText_channel_name.text.toString()
        var channelURL = editText_channel_url.text.toString()

        if (!URLUtil.isValidUrl(channelURL)) {
            message = getString(R.string.save_channel_invalid_url)
            readyToSave = false
        }

        if (channelName == "" || channelURL == "") {
            message = getString(R.string.save_channel_invalid_url)
            readyToSave = false
        }

        if (readyToSave) {

            val channelViewModel = ViewModelProvider(this).get(ChannelViewModel::class.java)

            if (mode == "new" || mode == "new_direct" ) {
                val channel = Channel(0, channelName, channelURL)
                channelViewModel.insert(channel)
                finish()

                firebaseAnalytics.logEvent("added_new_channel") {
                    param("channel_name", channelName)
                    param("channel_url", channelURL)
                }
            }

            if (mode == "edit") {
                channelViewModel.updateById(editChannelId, channelName, channelURL)
                finish()

                firebaseAnalytics.logEvent("editted_channel") {
                    param("channel_name", channelName)
                    param("channel_url", channelURL)
                }
            }

        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}