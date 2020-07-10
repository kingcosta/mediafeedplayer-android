package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Channel
import com.jppappstudio.mediafeedplayer.android.models.Listing
import com.jppappstudio.mediafeedplayer.android.ui.player.PlayerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.listing_row.view.*

class ListingsAdapter: RecyclerView.Adapter<ListingsViewHolder>(), Filterable {
    private var listings = emptyList<Listing>()
    private var listingsFiltered = mutableListOf<Listing>()
    private var listingsAll = emptyList<Listing>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.listing_row, parent, false)
        return ListingsViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return listings.size
    }

    override fun onBindViewHolder(holder: ListingsViewHolder, position: Int) {
        val listing = listings.get(position)

        holder.view.textView_listing_video_title.text = listing.title

        val imageView = holder.view.imageView_listing_thumbnail

        if (listing.thumbnailURL != "") {
            Picasso.get()
                .load(listing.thumbnailURL)
                .into(imageView)
        }

        if (!listing.bookmarkable) {
            holder.view.imageButton_favourite.visibility = View.GONE
        } else {
            // temporary turn off bookmark feature first
            holder.view.imageButton_favourite.visibility = View.GONE
        }

        holder.listing = listing
    }

    fun setListings(listings: List<Listing>) {
        this.listings = listings
        this.listingsAll = listings
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter = object : Filter() {

        // run on background thread
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            listingsFiltered.clear()

            if (charSequence.toString().isEmpty()) {
                listingsFiltered.addAll(listingsAll)
            } else {
                for (listing in listingsAll) {
                    if (listing.title.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        listingsFiltered.add(listing)
                    }
                }
            }

            val filterResult = FilterResults()
            filterResult.values = listingsFiltered
            return filterResult
        }

        // run on UI thread
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            listings = filterResults?.values as List<Listing>
            notifyDataSetChanged()
        }
    }
}

class ListingsViewHolder(val view: View, var listing: Listing? = null): RecyclerView.ViewHolder(view) {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    init {
        firebaseAnalytics = Firebase.analytics

        view.setOnClickListener {

            listing?.let {

                when (it.type) {
                    "application/rss+xml" -> {
                        var bundle = bundleOf(
                            "listingTitle" to it.title,
                            "listingURL" to it.url
                        )

                        Navigation.findNavController(view).navigate(R.id.openListingsFromListing, bundle)
                    }

                    "video/mp4" -> {

//                        if (InterstitialManager.mInterstitialAd.isLoaded()) {
//                            println("Interstitial Ads is loaded")
//                            InterstitialManager.mInterstitialAd.show()
//                        } else {
//                            println("Interstitial Ads is not ready")
//                        }

                        val intent = Intent(view.context, PlayerActivity::class.java)
                        intent.putExtra("videoURL", it.url)
                        view.context.startActivity(intent)

                        firebaseAnalytics.logEvent("play_video") {
                            param("source", "listings")
                        }
                    }

                    "web/html" -> {
                        val builder = CustomTabsIntent.Builder()
                        builder.setToolbarColor(ContextCompat.getColor(view.context, R.color.primaryColor))
                        builder.addDefaultShareMenuItem()

                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(view.context, Uri.parse(it.url))
                    }

                    else -> {}
                }
            }
        }
    }
}