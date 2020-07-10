package com.jppappstudio.mediafeedplayer.android.ui.listings

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jppappstudio.mediafeedplayer.android.R
import com.jppappstudio.mediafeedplayer.android.models.Listing
import kotlinx.android.synthetic.main.fragment_listings.view.*
import okhttp3.*
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.security.MessageDigest
import java.util.*

class ListingsFragment : Fragment() {

    private lateinit var viewModel: ListingsViewModel

    var listingTitle: String? = null
    var listingURL: String? = null
    var listings: List<Listing> = listOf()
    lateinit var progressBar: ProgressBar
    lateinit var searchView: SearchView

    private lateinit var viewAdapter: ListingsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        listingTitle = arguments?.getString("listingTitle")
        listingURL = arguments?.getString("listingURL")

        viewAdapter = ListingsAdapter()
        viewManager = LinearLayoutManager(activity)
        viewModel = ViewModelProvider(this).get(ListingsViewModel::class.java)
        println("Called ViewModelProvider")

        (activity as AppCompatActivity).supportActionBar?.title = listingTitle

        val root = inflater.inflate(R.layout.fragment_listings, container, false)

        root.recyclerview_listings.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
        }

        progressBar = root.progressBar_listing

        setHasOptionsMenu(true)

        fetchRecord()

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.listings_actionbar_menu, menu)
        var item = menu.findItem(R.id.listings_search)
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
                                var parser = ListingsXMLParser()

                                try {
                                    viewModel.listings = parser.parse(inputStreamBody)

                                    activity?.runOnUiThread {
                                        progressBar.visibility = View.INVISIBLE
                                        viewAdapter.setListings(viewModel.listings)
                                    }
                                } catch (exception: XmlPullParserException) {
                                    println("Parsing Exception: ${exception.localizedMessage}")
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
                        println("Fetch Record Network Failed: ${e.localizedMessage}")
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
            dialogBuilder.setMessage("Cannot connect to ${listingURL}!")
                .setCancelable(false)
                .setPositiveButton("Go Back") { _, _ ->
                    activity?.onBackPressed()
                }

            val alert = dialogBuilder.create()
            alert.setTitle("Opps")
            alert.show()
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