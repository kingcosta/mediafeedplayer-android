package com.jppappstudio.mediafeedplayer.android.services

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.jppappstudio.mediafeedplayer.android.BuildConfig

class InterstitialManager private constructor(context: Context){

    companion object : SingletonHolder<InterstitialManager, Context>(::InterstitialManager)

    var mInsterstitialAd: InterstitialAd
    private lateinit var onAdClosedHandler: () -> Unit

    init {
        mInsterstitialAd = InterstitialAd(context)
        mInsterstitialAd.adUnitId = BuildConfig.INTERSTITIAL_ADUNIT_ID

        mInsterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                onAdClosedHandler()
            }
        }
    }

    fun loadNewInterstitialAd(forceReload: Boolean = false) {
        if (!forceReload) {
            if (!mInsterstitialAd.isLoaded) {
                mInsterstitialAd.loadAd(AdRequest.Builder().build())
            }
        } else {
            mInsterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    fun setOnClosedHandler(handler: () -> Unit) {
        onAdClosedHandler = handler
    }
}