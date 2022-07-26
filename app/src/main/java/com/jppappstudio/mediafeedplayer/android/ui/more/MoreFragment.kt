package com.jppappstudio.mediafeedplayer.android.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.BuildConfig
import com.jppappstudio.mediafeedplayer.android.R

class MoreFragment : PreferenceFragmentCompat() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        firebaseAnalytics = Firebase.analytics

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        // Feedback Category
        val feedbackCategory = PreferenceCategory(context)
        feedbackCategory.key = "feedback_category"
        feedbackCategory.title = getString(R.string.more_feedback)
        screen.addPreference(feedbackCategory)

        val ratePreference = Preference(context)
        ratePreference.key = "rate"
        ratePreference.title = getString(R.string.more_rate)
        ratePreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        feedbackCategory.addPreference(ratePreference)

        val tellPreference = Preference(context)
        tellPreference.key = "tell"
        tellPreference.title = getString(R.string.more_tell)
        tellPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        feedbackCategory.addPreference(tellPreference)

        // Follow Us Category
        val followCategory = PreferenceCategory(context)
        followCategory.key = "follow_category"
        followCategory.title = getString(R.string.more_follow)
        screen.addPreference(followCategory)

        val fbPreference = Preference(context)
        fbPreference.key = "facebook"
        fbPreference.title = getString(R.string.more_facebook)
        fbPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        followCategory.addPreference(fbPreference)

        val sharePreference = Preference(context)
        sharePreference.key = "share"
        sharePreference.title = getString(R.string.more_share)
        sharePreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        followCategory.addPreference(sharePreference)

        // Other Category
        val otherCategory = PreferenceCategory(context)
        otherCategory.key = "other_category"
        otherCategory.title = getString(R.string.more_other)
        screen.addPreference(otherCategory)

        val userGuidePreference = Preference(context)
        userGuidePreference.key = "userguide"
        userGuidePreference.title = getString(R.string.more_userguide)
        userGuidePreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(userGuidePreference)

        val privacyPreference = Preference(context)
        privacyPreference.key = "privacy"
        privacyPreference.title = getString(R.string.more_privacy)
        privacyPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(privacyPreference)

        val termsPreference = Preference(context)
        termsPreference.key = "terms"
        termsPreference.title = getString(R.string.more_terms)
        termsPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(termsPreference)

        // Version Category
        val versionCategory = PreferenceCategory(context)
        versionCategory.key = "version_category"
        versionCategory.title = "${getString(R.string.more_version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        screen.addPreference(versionCategory)

        preferenceScreen = screen
    }

    fun onPreferenceClick(preference: Preference) {
        when (preference.key) {
            "rate", "tell", "facebook", "userguide", "privacy", "terms" -> {

                var url = ""

                when (preference.key) {
                    "rate" -> url = "https://play.google.com/store/apps/details?id=com.jppappstudio.mediafeedplayer.android"
                    "tell" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/contact-us"
                    "facebook" -> url = "https://www.facebook.com/mediafeedplayer"
                    "userguide" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/user-guide"
                    "privacy" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/privacy-policy"
                    "terms" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/terms-conditions"
                }

                val builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(ContextCompat.getColor(preferenceManager.context, R.color.primaryColor))
                builder.addDefaultShareMenuItem()

                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(preferenceManager.context, Uri.parse(url))
            }

            "share" -> {

                val share = Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/html"
                    putExtra(Intent.EXTRA_TEXT, "https://www.facebook.com/mediafeedplayer")
                    putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, null)

                startActivity(share)

            }

            else -> {}
        }

        firebaseAnalytics.logEvent("open_more_link") {
            param("url", preference.key)
        }
    }
}
