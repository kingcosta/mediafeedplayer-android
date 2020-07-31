package com.jppappstudio.mediafeedplayer.android.ui.favourites

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jppappstudio.mediafeedplayer.android.BuildConfig
import com.jppappstudio.mediafeedplayer.android.MainActivity
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.ui.listings.ListingsAdapter
import kotlinx.android.synthetic.main.fragment_favourites.view.*

class FavouritesFragment : Fragment() {

    private lateinit var viewAdapter: ListingsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var favouritesViewModel: FavouritesViewModel
    private lateinit var backgroundView: LinearLayout

    private var showBannerAds = BuildConfig.ALLOW_FAVOURITES_BANNER
    private lateinit var adView: AdView
    private lateinit var favouritesAdViewContainer: FrameLayout
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_favourites, container, false)

        viewAdapter = ListingsAdapter("favourites")
        viewManager = LinearLayoutManager(activity)

        root.recyclerview_favourites.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
        }

        backgroundView = root.findViewById(R.id.view_favourites_background)
        showBackgroundView()

        favouritesViewModel = ViewModelProvider(this).get(FavouritesViewModel::class.java)
        favouritesViewModel.allFavourites.observe(viewLifecycleOwner, Observer { favourites ->
            if (favourites.isNotEmpty()) {
                hideBackgroundView()
            } else {
                showBackgroundView()
            }

            viewAdapter.setListings(favourites)
        })
        viewAdapter.setFavouritesViewModel(favouritesViewModel)

        if (showBannerAds) {
            favouritesAdViewContainer = root.findViewById(R.id.favourites_ad_view_container)
            constraintLayout = root.findViewById(R.id.favourites_constraintlayout)
            loadBanner()
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).setFavouriteBadge(remove = true)
    }

    private fun hideBackgroundView() {
        backgroundView.visibility = View.GONE
    }

    private fun showBackgroundView() {
        backgroundView.visibility = View.VISIBLE
    }

    private fun loadBanner() {
        adView = AdView(context)
        adView.adSize = getAdaptiveBannerSize(requireContext(), favouritesAdViewContainer.width.toFloat())
        adView.adUnitId = BuildConfig.FAVOURITES_ADUNIT_ID

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                favouritesAdViewContainer.addView(adView)

                val animation = AnimationUtils.loadAnimation(context, R.anim.banner_slideup)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(constraintLayout)
                        constraintSet.connect(R.id.recyclerview_favourites, ConstraintSet.BOTTOM, R.id.favourites_ad_view_container, ConstraintSet.TOP, 0)
                        constraintSet.applyTo(constraintLayout)
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationStart(p0: Animation?) {}
                })


                favouritesAdViewContainer.startAnimation(animation)
            }
        }

        adView.loadAd(AdRequest.Builder().build())
    }

    private fun getAdaptiveBannerSize(context: Context, width: Float): AdSize {
        val display = activity?.windowManager?.defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = width
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        val adSize =  AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

        return adSize
    }
}