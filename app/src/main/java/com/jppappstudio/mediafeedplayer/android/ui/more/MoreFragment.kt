package com.jppappstudio.mediafeedplayer.android.ui.more

import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.jppappstudio.mediafeedplayer.android.BuildConfig
import com.jppappstudio.mediafeedplayer.android.R

class MoreFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        // Feedback Category
        val feedbackCategory = PreferenceCategory(context)
        feedbackCategory.key = "feedback_category"
        feedbackCategory.title = "Feedback"
        screen.addPreference(feedbackCategory)

//        val ratePreference = Preference(context)
//        ratePreference.key = "rate"
//        ratePreference.title = "Rate us on Play Store"
//        ratePreference.setOnPreferenceClickListener {
//            onPreferenceClick(it)
//            true
//        }
//        feedbackCategory.addPreference(ratePreference)

        val tellPreference = Preference(context)
        tellPreference.key = "tell"
        tellPreference.title = "Tell us your feedback"
        tellPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        feedbackCategory.addPreference(tellPreference)

        // Follow Us Category
        val followCategory = PreferenceCategory(context)
        followCategory.key = "follow_category"
        followCategory.title = "Follow Us"
        screen.addPreference(followCategory)

        val fbPreference = Preference(context)
        fbPreference.key = "facebook"
        fbPreference.title = "Facebook"
        fbPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        followCategory.addPreference(fbPreference)

//        val sharePreference = Preference(context)
//        sharePreference.key = "share"
//        sharePreference.title = "Share with friends"
//        sharePreference.setOnPreferenceClickListener {
//            onPreferenceClick(it)
//            true
//        }
//        followCategory.addPreference(sharePreference)

        // Other Category
        val otherCategory = PreferenceCategory(context)
        otherCategory.key = "other_category"
        otherCategory.title = "Other"
        screen.addPreference(otherCategory)

        val userGuidePreference = Preference(context)
        userGuidePreference.key = "userguide"
        userGuidePreference.title = "User Guide"
        userGuidePreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(userGuidePreference)

        val privacyPreference = Preference(context)
        privacyPreference.key = "privacy"
        privacyPreference.title = "Privacy Policy"
        privacyPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(privacyPreference)

        val termsPreference = Preference(context)
        termsPreference.key = "terms"
        termsPreference.title = "Terms & Conditions"
        termsPreference.setOnPreferenceClickListener {
            onPreferenceClick(it)
            true
        }
        otherCategory.addPreference(termsPreference)

        // Version Category
        val versionCategory = PreferenceCategory(context)
        versionCategory.key = "version_category"
        versionCategory.title = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        screen.addPreference(versionCategory)

        preferenceScreen = screen
    }

    fun onPreferenceClick(preference: Preference) {
        when (preference.key) {
            "rate", "tell", "facebook", "userguide", "privacy", "terms" -> {

                var url = ""

                when (preference.key) {
                    "rate" -> url = "https://apps.apple.com/us/app/media-feed-player/id1516148350?action=write-review"
                    "tell" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/contact-us"
                    "facebook" -> url = "https://www.facebook.com/mediafeedplayer"
                    "userguide" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/user-guide"
                    "privacy" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/privacy-policy"
                    "terms" -> url = "https://sites.google.com/view/jppappstudio-media-feed-player/terms-conditions"
                }

                val builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(ContextCompat.getColor(preferenceManager.context, R.color.colorPrimary))
                builder.addDefaultShareMenuItem()

                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(preferenceManager.context, Uri.parse(url))
            }

            "share" -> {

            }

            else -> {}
        }
    }
}
