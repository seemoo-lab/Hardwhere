package com.heinecke.aron.LARS.ui.scan

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.SearchResults
import com.heinecke.aron.LARS.ui.APIFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SelectorFragment.OnListFragmentInteractionListener] interface.
 */
class AssetSearchFragment : APIFragment(),
    AssetRecyclerViewAdapter.OnListFragmentInteractionListener,
    SearchView.OnQueryTextListener {

    // currently selected item or null
    private lateinit var viewModel: ScanViewModel
    private lateinit var adapter: AssetRecyclerViewAdapter
    private var returnCode: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.run { viewModel.scanList.value!!.addAll(this.getParcelableArrayList(S_SCAN_LIST)!!) }
        adapter = AssetRecyclerViewAdapter(this,viewModel.searchResults.value!!)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@AssetSearchFragment.adapter
            }
        }

        viewModel = ViewModelProviders.of(requireActivity())[ScanViewModel::class.java]
        viewModel.searchString.observe(viewLifecycleOwner, Observer {
            val api = getAPI()
            if (it != null && it.isNotBlank()) {
//                api.searchSelectable(selectType.getTypeName(),it).enqueue(SearchResultCallback(requireContext(),selectType,adapter))
            } else {

            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_list, container, false)
        setHasOptionsMenu(true)
        // Set the adapter
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(S_SCAN_LIST,viewModel.searchResults.value)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select, menu)
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            setOnQueryTextListener(this@AssetSearchFragment)
            setQuery(viewModel.searchString.value,false)
            isIconified = false
        }
    }

    companion object {
        const val S_SCAN_LIST: String = "scan_list"

        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstanceID(): Int {
            return R.id.asset_search_fragment
        }
    }

    override fun onListFragmentInteraction(item: Asset) {
        Log.d(this@AssetSearchFragment::class.java.name,"Selected: $item")
        if (viewModel.scanList.value!!.any { asset: Asset ->  asset.id == id }) {
            Toast.makeText(context,R.string.duplicate_manual, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.scanList.value!!.add(item)
            findNavController().popBackStack()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchString.value = query
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchString.value = newText
        return true
    }

    class SearchResultCallback(val context: Context, val adapter: AssetRecyclerViewAdapter) : Callback<SearchResults<Asset>> {
        override fun onFailure(call: Call<SearchResults<Asset>>?, t: Throwable?) {
            Log.w(this::class.java.name, "$t")
            Toast.makeText(context,R.string.error_fetch_selectable,Toast.LENGTH_SHORT).show()
        }

        override fun onResponse(
            call: Call<SearchResults<Asset>>?,
            response: Response<SearchResults<Asset>>?
        ) {
            response?.run {
                val elements = this.body()!!.rows
                if (this.isSuccessful) {
                    Log.d(this::class.java.name, "Body: $elements")
                    adapter.replaceElements(elements)
                } else {
                    Toast.makeText(
                        context,
                        R.string.error_fetch_selectable,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } ?: Utils.logResponseVerbose(this::class.java, response)
        }
    }

}
