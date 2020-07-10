package com.jppappstudio.mediafeedplayer.android.ui.channels

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Channel
import com.jppappstudio.mediafeedplayer.android.models.ChannelViewModel
import kotlinx.android.synthetic.main.channel_row.view.*
import kotlinx.android.synthetic.main.fragment_channels.view.*

class ChannelsFragment : Fragment() {

    private lateinit var viewAdapter: ChannelsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var channelViewModel: ChannelViewModel

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

        channelViewModel = ViewModelProvider(this).get(ChannelViewModel::class.java)
        channelViewModel.allChannels.observe(viewLifecycleOwner, Observer { channels ->
            channels.let {
                viewAdapter.setChannels(it)
            }
        })

        viewAdapter.setChannelViewModel(channelViewModel)

        setupFab(root.floatingActionButton_new_channel)
        return root
    }

    fun setupFab(fab: FloatingActionButton) {
        fab.setOnClickListener {
            val intent = Intent(context, NewChannelActivity::class.java)
            context?.startActivity(intent)
        }
    }
}

class ChannelsAdapter: RecyclerView.Adapter<ChannelsViewHolder>() {

    private var channels = emptyList<Channel>()
    private lateinit var channelViewModel: ChannelViewModel

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
            var popupMenu = PopupMenu(it.context, holder.view.imageButton_more)
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
                "listingTitle" to channel?.name,
                "listingURL" to channel?.url
            )

            Navigation.findNavController(view).navigate(R.id.openListingsFromChannel, bundle)
        }


    }
}