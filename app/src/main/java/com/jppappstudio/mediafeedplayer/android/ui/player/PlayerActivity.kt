package com.jppappstudio.mediafeedplayer.android.ui.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.services.InterstitialManager

class PlayerActivity: AppCompatActivity(), Player.EventListener {

    lateinit var playerView: PlayerView
    lateinit var progressBar: ProgressBar
    lateinit var btFullScreen: ImageView
    lateinit var btBack: ImageView
    lateinit var simpleExoPlayer: SimpleExoPlayer
    var flag = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // assign variable
        playerView = findViewById(R.id.player_view)
        progressBar = findViewById(R.id.progress_bar)
        btFullScreen = playerView.findViewById(R.id.bt_fullscreen)
        btBack = playerView.findViewById(R.id.bt_back)

        hideSystemUI()
        setupPlayer()
    }

    override fun onRestart() {
        super.onRestart()
        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.release()
    }

    private fun setupPlayer() {
        // Video url
        val videoUrl = Uri.parse(intent.getStringExtra("videoURL"))

        // Initialize load control
        val loadControl = DefaultLoadControl()
        // Initialize band width meter
        val bandwidthMeter = DefaultBandwidthMeter()
        // Initialize track selector
        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
        // Initialize simple exo player
        simpleExoPlayer =
            ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelector, loadControl)
        // Initialize data source factory
        val factory = DefaultHttpDataSourceFactory("exoplayer_video", 5000, 5000, true)
        val extractorsFactory = DefaultExtractorsFactory()
        // Initialize media source
        val mediaSource = ExtractorMediaSource(videoUrl, factory, extractorsFactory, null, null)

        // set player
        playerView.setPlayer(simpleExoPlayer)
        // keep screen on
        playerView.keepScreenOn = true
        // prepare media
        simpleExoPlayer.prepare(mediaSource)
        // play video when ready
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.addListener(this)

        btFullScreen.setOnClickListener(View.OnClickListener {
            if (flag) {
                // when flag is true
                // set enter full screen image
                btFullScreen.setImageResource(R.drawable.ic_fullscreen)
                // set portrait orientation
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                // set flag value is false
                flag = false
            } else {
                // when flag is false
                btFullScreen.setImageResource(R.drawable.ic_fullscreen_exit)
                // set landscape orientation
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                // set flag value is true
                flag = true
            }
        })

        btBack.setOnClickListener(View.OnClickListener {
            InterstitialManager.getInstance(this).loadNewInterstitialAd()
            finish()
        })
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        // check condition
        if (playbackState == Player.STATE_BUFFERING) {
            // when buffering, show progress bar
            progressBar.visibility = View.VISIBLE
        } else if (playbackState == Player.STATE_READY) {
            // when ready, hide progress bar
            progressBar.visibility = View.GONE
        }
    }

    fun hideSystemUI() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                // or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                // or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
}