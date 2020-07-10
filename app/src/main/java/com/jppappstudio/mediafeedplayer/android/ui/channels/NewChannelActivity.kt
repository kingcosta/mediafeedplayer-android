package com.jppappstudio.mediafeedplayer.android.ui.channels

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Channel
import com.jppappstudio.mediafeedplayer.android.models.ChannelViewModel
import kotlinx.android.synthetic.main.add_new_channel.*

class NewChannelActivity: AppCompatActivity() {

    var mode = "new"
    var editChannelId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            println("Saved instance is not null")
        } else {
            println("No saved instance")
        }

        setContentView(R.layout.add_new_channel)
        editText_channel_name.requestFocus()

        val bundle: Bundle? = intent.extras
        var newMode: String? = bundle?.getString("mode")

        if (newMode != null) {
            mode = newMode
        }

        if (mode == "edit") {
            supportActionBar?.title = "Edit Channel"

            editChannelId = bundle?.getInt("id")!!

            textView_channal_name.text = "New Channel Name"
            textView_channel_url.text = "New Channel URL"

            editText_channel_name.setText(bundle.getString("name"))
            editText_channel_url.setText(bundle.getString("url"))
        }
    }

    override fun onStart() {
        super.onStart()
        checkForDynamicLinks()
    }

    private fun checkForDynamicLinks() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                if (deepLink != null) {
                    // handle deep link
                    val deepLinkAction = deepLink.getQueryParameter("action")

                    if (deepLinkAction != "new_channel") {
                        println("Malicious deep link")
                    } else {
                        val channelName = deepLink.getQueryParameter("name")
                        val channelURL = deepLink.getQueryParameter("url")

                        editText_channel_name.setText(channelName)
                        editText_channel_url.setText(channelURL)
                    }
                }
            }

            .addOnFailureListener(this) { e ->
                println("getDynamicLink:OnFailure – ${e.localizedMessage}")
            }
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
            message = "Please make sure you fill in correct URL."
            readyToSave = false
        }

        if (channelName == "" || channelURL == "") {
            message = "Please fill in all information. "
            readyToSave = false
        }

        if (readyToSave) {

            val channelViewModel = ViewModelProvider(this).get(ChannelViewModel::class.java)

            if (mode == "new") {
                val channel = Channel(0, channelName, channelURL)
                channelViewModel.insert(channel)
                finish()
            }

            if (mode == "edit") {
                channelViewModel.updateById(editChannelId, channelName, channelURL)
                finish()
            }

        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}