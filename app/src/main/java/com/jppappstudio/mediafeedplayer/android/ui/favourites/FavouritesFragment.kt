package com.jppappstudio.mediafeedplayer.android.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jppappstudio.mediafeedplayer.android.R
import kotlinx.android.synthetic.main.fragment_favourites.view.*

class FavouritesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_favourites, container, false)
        return root
    }
}