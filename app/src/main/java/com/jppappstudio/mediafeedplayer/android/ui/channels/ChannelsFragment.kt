package com.jppappstudio.mediafeedplayer.android.ui.channels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.jppappstudio.mediafeedplayer.android.BuildConfig
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Channel
import kotlinx.android.synthetic.main.channel_row.view.*
import kotlinx.android.synthetic.main.fragment_channels.*
import kotlinx.android.synthetic.main.fragment_channels.view.*

class ChannelsFragment : Fragment() {

    private lateinit var viewAdapter: ChannelsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var channelViewModel: ChannelViewModel
    private lateinit var backgroundView: LinearLayout

    private var showBannerAds = BuildConfig.ALLOW_CHANNELS_BANNER
    private lateinit var adView: AdView
    private lateinit var channelsAdViewContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_channels, container, false)

        viewAdapter = ChannelsAdapter()
        viewManager = LinearLayoutManager(activity)
        root.recyclerview_channels_list.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
        }

        backgroundView = root.findViewById(R.id.view_channels_background)
        showBackgroundView()

        channelViewModel = ViewModelProvider(this).get(ChannelViewModel::class.java)
        channelViewModel.allChannels.observe(viewLifecycleOwner, Observer { channels ->
            channels.let {
                if (it.isNotEmpty()) {
                    hideBackgroundView()
                } else {
                    showBackgroundView()
                }

                viewAdapter.setChannels(it)
            }
        })

        viewAdapter.setChannelViewModel(channelViewModel)

        setupFab(root.floatingActionButton_new_channel)

        if (showBannerAds) {
            channelsAdViewContainer = root.findViewById(R.id.channels_ad_view_container)
            loadBanner()
        }

        return root
    }

    private fun setupFab(fab: FloatingActionButton) {
        fab.setOnClickListener {
            val intent = Intent(context, NewChannelActivity::class.java)
            context?.startActivity(intent)
        }
    }

    private fun hideBackgroundView() {
        backgroundView.visibility = View.GONE
    }

    private fun showBackgroundView() {
        backgroundView.visibility = View.VISIBLE
    }

    private fun loadBanner() {
        adView = AdView(context)
        adView.adSize = getAdaptiveBannerSize(requireContext(), channelsAdViewContainer.width.toFloat())
        adView.adUnitId = BuildConfig.CHANNELS_ADUNIT_ID
        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                channelsAdViewContainer.addView(adView)

                val animation = AnimationUtils.loadAnimation(context, R.anim.banner_slideup)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        val constraintLayout = channels_constraintlayout
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(constraintLayout)
                        constraintSet.connect(R.id.recyclerview_channels_list, ConstraintSet.BOTTOM, R.id.channels_ad_view_container, ConstraintSet.TOP, 0)
                        constraintSet.applyTo(constraintLayout)
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationStart(p0: Animation?) {}
                })
                channelsAdViewContainer.startAnimation(animation)
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

class ChannelsAdapter: RecyclerView.Adapter<ChannelsViewHolder>() {

    private var firebaseAnalytics: FirebaseAnalytics
    private var channels = emptyList<Channel>()
    private lateinit var channelViewModel: ChannelViewModel

    init {
        firebaseAnalytics = Firebase.analytics
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.channel_row, parent, false)
        return ChannelsViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    override fun onBindViewHolder(holder: ChannelsViewHolder, position: Int) {
        val channel = channels.get(position)

        holder.channel = channel
        holder.view.textView_channel_name.text = channel.name
        holder.view.imageButton_more.setOnClickListener {
            val popupMenu = PopupMenu(it.context, holder.view.imageButton_more)
            popupMenu.inflate(R.menu.channel_row_menu)
            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {
                    R.id.action_edit -> {

                        val intent = Intent(holder.view.context, NewChannelActivity::class.java)
                        intent.putExtra("mode", "edit")
                        intent.putExtra("id", channel.id)
                        intent.putExtra("name", channel.name)
                        intent.putExtra("url", channel.url)
                        holder.view.context.startActivity(intent)

                        true
                    }

                    R.id.action_delete -> {
                        channelViewModel.delete(channel)

                        firebaseAnalytics.logEvent("deleted_channel") {
                            param("channel_name", channel.name)
                            param("channel_url", channel.url)
                        }

                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }
    }

    fun setChannelViewModel(viewModel: ChannelViewModel) {
        this.channelViewModel = viewModel
    }

    fun setChannels(channels: List<Channel>) {
        this.channels = channels
        notifyDataSetChanged()
    }
}

class ChannelsViewHolder(val view: View, var channel: Channel? = null): RecyclerView.ViewHolder(view) {

    init {
        view.setOnClickListener {
            val bundle = bundleOf(
                "source" to "listings",
                "listingTitle" to channel?.name,
                "listingURL" to channel?.url
            )

            Navigation.findNavController(view).navigate(R.id.openListingsFromChannel, bundle)
        }


    }
}