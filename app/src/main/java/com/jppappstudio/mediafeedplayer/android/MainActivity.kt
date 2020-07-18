package com.jppappstudio.mediafeedplayer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.extensions.setupWithNavController
import com.jppappstudio.mediafeedplayer.android.services.InterstitialManager
import com.jppappstudio.mediafeedplayer.android.ui.channels.NewChannelActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var showAds = BuildConfig.ALLOW_NAVIGATION_BANNER
    private lateinit var adView: AdView
    private lateinit var navAdViewContainer: FrameLayout

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        firebaseAnalytics = Firebase.analytics

        // Ugly Hack. More context at
        // https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
        // https://issuetracker.google.com/issues/36907463
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action.equals(Intent.ACTION_MAIN)) {
            finish()
            return
        }

        // Nexus: 32458C77D0FBD29C560661E15833A002
        val testDeviceIds = listOf("")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(this) {}

        if (showAds) {
            navAdViewContainer = findViewById(R.id.nav_ad_view_container)
            loadBanner()
        }

        checkForDynamicLinks()
    }

    override fun onStart() {
        super.onStart()
        InterstitialManager.getInstance(this).loadNewInterstitialAd()
    }

    private fun checkForDynamicLinks() {
        var channelName: String? = ""
        var channelURL: String? = ""
        var action: String? = ""

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deepLink: Uri? = null

                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link

                    if (deepLink != null) {
                        // println("Deeplink: Firebase Deep Link")

                        action = deepLink.getQueryParameter("action")

                        if (action != "new_channel") {
                            // println("Malicious deeplink")
                        } else {
                            channelName = deepLink.getQueryParameter("name")
                            channelURL = deepLink.getQueryParameter("url")
                        }
                    }
                } else if (intent.dataString != null) {
                    // println("Deeplink: Regular Deep Link")

                    val uri = Uri.parse(intent.dataString)
                    action = uri.getQueryParameter("action")

                    if (action != "new_channel") {
                        // println("Malicious deeplink")
                    } else {
                        channelName = uri.getQueryParameter("name")
                        channelURL = uri.getQueryParameter("url")
                    }
                }

                if (action == "new_channel" || channelName != "" || channelURL != "") {
                    val newIntent = Intent(this, NewChannelActivity::class.java)
                    newIntent.putExtra("mode", "new_direct")
                    newIntent.putExtra("name", channelName)
                    newIntent.putExtra("url", channelURL)
                    startActivity(newIntent)
                }
            }

            .addOnFailureListener(this) { e ->
                // println("getDynamicLink:OnFailure – ${e.localizedMessage}")
            }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraphIds = listOf(
            R.navigation.nav_channels,
            R.navigation.nav_favourites,
            R.navigation.nav_more
        )

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

    fun setFavouriteBadge(remove: Boolean = false) {
        bottomNavigationView.getOrCreateBadge(R.id.nav_favourites).apply {
            backgroundColor = ContextCompat.getColor(applicationContext, R.color.primaryDarkColor)
            isVisible = !remove
        }
    }

    private fun loadBanner() {
        adView = AdView(this)
        adView.adSize = getAdaptiveBannerSize(navAdViewContainer.width.toFloat())
        adView.adUnitId = BuildConfig.NAVIGATION_ADUNIT_ID

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                navAdViewContainer.addView(adView)

                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.banner_slideup)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        val constraintLayout = container
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

    fun getAdaptiveBannerSize(width: Float): AdSize {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = width
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        val adSize =  AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)

        return adSize
    }
}
