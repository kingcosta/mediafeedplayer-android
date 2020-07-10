package com.jppappstudio.mediafeedplayer.android.services

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

object InterstitialManager {

    lateinit var mInterstitialAd: InterstitialAd

    fun createInterstitial(context: Context) {
        println("Create interstitial called")

        if (!this::mInterstitialAd.isInitialized) {
            println("Interstitial is not initialized")

            mInterstitialAd = InterstitialAd(context)
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
            mInterstitialAd.adListener = object: AdListener() {
                override fun onAdLoaded() {
                    println("Interstitial Ads Loaded")
                }

                override fun onAdClosed() {
                    println("Interstitial Ads Closed")

                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
            mInterstitialAd.loadAd(AdRequest.Builder().build())

        } else {
            println("Interstitial is initialized")

            if (!mInterstitialAd.isLoaded) {
                println("Interstitial is initialized but not loaded")

                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }
}