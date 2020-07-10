package com.jppappstudio.mediafeedplayer.android

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.fragment_channels.*
import com.jppappstudio.mediafeedplayer.android.extensions.setupWithNavController

class MainActivity : AppCompatActivity() {

    private var showAds = false
    private lateinit var adView: AdView
    private lateinit var navAdViewContainer: FrameLayout

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("Main Activity OnCreate Triggered")

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        // Ugly Hack. More context at
        // https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
        // https://issuetracker.google.com/issues/36907463
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action.equals(Intent.ACTION_MAIN)) {
            finish()
            return
        }

        if (showAds) {
            MobileAds.initialize(this) {
                navAdViewContainer = findViewById(R.id.nav_ad_view_container)
                loadBanner()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraphIds = listOf(R.navigation.nav_channels, R.navigation.nav_more)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, androidx.lifecycle.Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    private fun loadBanner() {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = navAdViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        val adSize =  AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)

        adView = AdView(this)
        adView.adSize = adSize
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                navAdViewContainer.addView(adView)

                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.banner_slideup)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        val constraintLayout = channels_constraintlayout
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(constraintLayout)
                        constraintSet.connect(R.id.nav_host_container, ConstraintSet.BOTTOM, R.id.nav_ad_view_container, ConstraintSet.TOP, 0)
                        constraintSet.applyTo(constraintLayout)
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationStart(p0: Animation?) {}
                })
                navAdViewContainer.startAnimation(animation)
            }
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
