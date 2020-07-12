package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.BuildConfig
import com.jppappstudio.mediafeedplayer.android.MainActivity
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Listing
import kotlinx.android.synthetic.main.fragment_channels.*
import kotlinx.android.synthetic.main.fragment_listings.*
import kotlinx.android.synthetic.main.fragment_listings.view.*
import okhttp3.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.security.MessageDigest
import java.util.*

class ListingsFragment : Fragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var viewModel: ListingsViewModel

    var listingTitle: String? = null
    var listingURL: String? = null
    lateinit var progressBar: ProgressBar
    lateinit var searchView: SearchView

    private lateinit var viewAdapter: ListingsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var showBannerAds = BuildConfig.ALLOW_LISTINGS_BANNER
    private lateinit var adView: AdView
    private lateinit var listingsAdViewContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAnalytics = Firebase.analytics

        viewAdapter = ListingsAdapter()
        viewManager = LinearLayoutManager(activity)
        viewModel = ViewModelProvider(this).get(ListingsViewModel::class.java)

        listingTitle = arguments?.getString("listingTitle")
        listingURL = arguments?.getString("listingURL")

        viewModel.listingTitle = listingTitle ?: "Videos"
        (activity as MainActivity).supportActionBar?.title = viewModel.listingTitle

        val root = inflater.inflate(R.layout.fragment_listings, container, false)

        root.recyclerview_listings.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
        }

        progressBar = root.progressBar_listing
        setHasOptionsMenu(true)
        fetchRecord()

        if (showBannerAds) {
            listingsAdViewContainer = root.findViewById(R.id.listings_ad_view_container)
            loadBanner()
        }

        return root
    }

    private fun loadBanner() {
        adView = AdView(context)
        adView.adSize = getAdaptiveBannerSize(requireContext(), listingsAdViewContainer.width.toFloat())
        adView.adUnitId = BuildConfig.LISTINGS_ADUNIT_ID

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                listingsAdViewContainer.addView(adView)

                val animation = AnimationUtils.loadAnimation(context, R.anim.banner_slideup)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        val constraintLayout = listings_constraintlayout
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(constraintLayout)
                        constraintSet.connect(R.id.recyclerview_listings, ConstraintSet.BOTTOM, R.id.listings_ad_view_container, ConstraintSet.TOP, 0)
                        constraintSet.applyTo(constraintLayout)
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationStart(p0: Animation?) {}
                })


                listingsAdViewContainer.startAnimation(animation)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.listings_actionbar_menu, menu)
        val item = menu.findItem(R.id.listings_search)
        searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewAdapter.getFilter().filter(newText)
                return false
            }
        })
    }

    fun fetchRecord() {
        progressBar.visibility = View.VISIBLE

        if (viewModel.listings.isEmpty()) {
            val uid = UUID.randomUUID().toString().toMD5()

            if (listingURL != null) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(listingURL.toString())
                    .addHeader("UID", uid)
                    .build()

                client.newCall(request).enqueue(object: Callback {
                    override fun onResponse(call: Call, response: Response) {
                        if (response.body?.contentType().toString().contains("xml")) {
                            val inputStreamBody = response.body?.byteStream()

                            if (inputStreamBody != null) {
                                val parser = ListingsXMLParser()

                                try {
                                    viewModel.listings = parser.parse(inputStreamBody)

                                    activity?.runOnUiThread {
                                        progressBar.visibility = View.INVISIBLE
                                        viewAdapter.setListings(viewModel.listings)
                                    }
                                } catch (exception: XmlPullParserException) {
                                    // println("Parsing Exception: ${exception.localizedMessage}")
                                    showConnectionFailureDialog()
                                }
                            } else {
                                showConnectionFailureDialog()
                            }
                        } else {
                            showConnectionFailureDialog()
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        // println("Fetch Record Network Failed: ${e.localizedMessage}")
                        showConnectionFailureDialog()
                    }
                })
            }
        } else {
            activity?.runOnUiThread {
                progressBar.visibility = View.INVISIBLE
                viewAdapter.setListings(viewModel.listings)
            }
        }
    }

    fun showConnectionFailureDialog() {
        activity?.runOnUiThread {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage(getString(R.string.listings_cannot_connect) + listingURL + getString(R.string.listings_please_check))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.listings_go_back)) { _, _ ->
                    activity?.onBackPressed()
                }

            val alert = dialogBuilder.create()
            alert.setTitle(getString(R.string.listings_cannot_connect_header))
            alert.show()

            firebaseAnalytics.logEvent("cannot_connect_to_feed") {
                param("feed_url", listingURL!!)
            }
        }
    }

    fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
    }

    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}