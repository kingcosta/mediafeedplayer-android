package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.MainActivity
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Listing
import com.jppappstudio.mediafeedplayer.android.services.InterstitialManager
import com.jppappstudio.mediafeedplayer.android.ui.favourites.FavouritesViewModel
import com.jppappstudio.mediafeedplayer.android.ui.player.PlayerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.listing_row.view.*

class ListingsAdapter(private var mode: String = "listings"): RecyclerView.Adapter<ListingsViewHolder>(), Filterable {

    private var listings = emptyList<Listing>()
    private var listingsFiltered = mutableListOf<Listing>()
    private var listingsAll = emptyList<Listing>()

    private lateinit var listingsViewModel: ListingsViewModel
    private lateinit var favouritesViewModel: FavouritesViewModel

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

        holder.listing = listing
        holder.view.textView_listing_video_title.text = listing.title

        val imageView = holder.view.imageView_listing_thumbnail
        if (listing.thumbnailURL != "") {
            Picasso.get()
                .load(listing.thumbnailURL)
                .into(imageView)
        }

        holder.view.imageButton_favourite.apply {
            setImageResource(R.drawable.ic_bookmark_border_black_24dp)

            if (mode == "listings") {
                if (favouritesViewModel.favourited.get(position)) {
                    setImageResource(R.drawable.ic_bookmark_black_24dp)
                } else {
                    setImageResource(R.drawable.ic_bookmark_border_black_24dp)

                    setOnClickListener {
                        favouritesViewModel.insert(listing)
                        favouritesViewModel.favourited[position] = true
                        setImageResource(R.drawable.ic_bookmark_black_24dp)
                        (context as MainActivity).setFavouriteBadge()

                        Firebase.analytics.logEvent("added_favourite") {
                            param("content_title", listing.title)
                            param("content_type", listing.type)
                        }
                    }
                }

                if (!listing.bookmarkable) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                }
            }

            if (mode == "favourites") {
                visibility = View.VISIBLE
                setImageResource(R.drawable.ic_bookmark_black_24dp)
                setOnClickListener {
                    favouritesViewModel.delete(listing)
                    Firebase.analytics.logEvent("deleted_favourite") {
                        param("content_title", listing.title)
                        param("content_type", listing.type)
                    }
                }
            }
        }
    }

    fun setListings(listings: List<Listing>) {
        this.listings = listings
        this.listingsAll = listings

        if (this.favouritesViewModel.favourited.isEmpty()) {
            this.favouritesViewModel.favourited = MutableList(listings.size) { false }
        }

        notifyDataSetChanged()
    }

    fun setListingsViewModel(viewModel: ListingsViewModel) {
        this.listingsViewModel = viewModel
    }

    fun setFavouritesViewModel(viewModel: FavouritesViewModel) {
        this.favouritesViewModel = viewModel
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

    init {
        view.setOnClickListener {
            listing?.let {
                when (it.type) {
                    "application/rss+xml" -> {
                        val bundle = bundleOf(
                            "source" to "listings",
                            "listingTitle" to it.title,
                            "listingURL" to it.url
                        )

                        Navigation.findNavController(view).navigate(R.id.openListingsFromListing, bundle)
                    }

                    "video/mp4" -> {
                        val startPlayer = {
                            val intent = Intent(view.context, PlayerActivity::class.java)
                            intent.putExtra("videoURL", it.url)
                            view.context.startActivity(intent)

                            Firebase.analytics.logEvent("play_video") {
                                param("source", "listings")
                            }
                        }

                        startPlayer()
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

                Firebase.analytics.logEvent("play_content") {
                    param("content_title", it.title)
                    param("content_type", it.type)
                }
            }
        }
    }
}