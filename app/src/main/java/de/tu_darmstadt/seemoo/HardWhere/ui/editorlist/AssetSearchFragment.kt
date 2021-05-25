package de.tu_darmstadt.seemoo.HardWhere.ui.editorlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.Utils.Companion.hideKeyboard
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.data.model.SearchResults
import de.tu_darmstadt.seemoo.HardWhere.ui.APIFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Asset search fragment.
 * Uses the [EditorListViewModel]
 */
class AssetSearchFragment : APIFragment(),
    AssetRecyclerViewAdapter.OnListInteractionListener,
    SearchView.OnQueryTextListener {

    // currently selected item or null
    private lateinit var viewModel: EditorListViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: AssetRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[EditorListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[EditorListViewModel::class.java]
        savedInstanceState?.run {
            viewModel.searchString.value = getString(S_SEARCH_STRING)
        }
        adapter = AssetRecyclerViewAdapter(this, arrayListOf())
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshContainer)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AssetSearchFragment.adapter
        }
        viewModel.run {
            swipeRefreshLayout.setOnRefreshListener { updateData(searchString.value) }
            searchString.observe(viewLifecycleOwner, Observer {
                updateData(it)
            })
            resolving.observe(viewLifecycleOwner, Observer {
                swipeRefreshLayout.isRefreshing = it != 0
            })
            searchFiltered.observe(viewLifecycleOwner, Observer {
                adapter.replaceElements(it)
            })
        }
    }

    private fun updateData(data: String?) {
        viewModel.run {
            cancelNetworkCall()
            val api = getAPI()
            if (data != null && data.isNotBlank()) {
                incLoading()
                val call =
                api.searchAsset(data)
                setNetworkCall(call)
                call.enqueue(SearchResultCallback(requireContext(),adapter, this))
            } else {
                adapter.clearItems()
            }
        }
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
        outState.putString(S_SEARCH_STRING, viewModel.searchString.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.filter) {
            val filterPopup = AssetFilterBTFragment()
            filterPopup.show(parentFragmentManager,filterPopup.tag)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select, menu)
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).run {
            this.setIconifiedByDefault(false)
            setOnQueryTextListener(this@AssetSearchFragment)
            setQuery(viewModel.searchString.value, false)
            isIconified = false
        }
        (menu.findItem(R.id.filter) as MenuItem).run {
            setVisible(true)
        }
    }

    companion object {
        const val S_SEARCH_STRING: String = "search_input"

        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstanceID(): Int {
            return R.id.asset_search_fragment
        }
    }

    override fun onListItemClicked(item: Asset) {
        Log.d(this@AssetSearchFragment::class.java.name, "Selected: $item")
        if (viewModel.scanList.value!!.any { asset: Asset -> asset.id == id }) {
            Toast.makeText(context, R.string.duplicate_manual, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.scanList.value!!.add(item)
            hideKeyboard(requireActivity())
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

    class SearchResultCallback(val context: Context, private val adapter: AssetRecyclerViewAdapter, private val viewModel: EditorListViewModel) :
        Callback<SearchResults<Asset>> {
        override fun onFailure(call: Call<SearchResults<Asset>>?, t: Throwable?) {
            viewModel.decLoading()
            if(!call!!.isCanceled) {
                Log.w(this::class.java.name, "$t")
                Toast.makeText(context, R.string.error_fetch_selectable, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(
            call: Call<SearchResults<Asset>>?,
            response: Response<SearchResults<Asset>>?
        ) {
            viewModel.decLoading()
            response?.run {
                if (this.isSuccessful) {
                    viewModel.searchFetchData.value = this.body()!!.rows
                } else {
                    Utils.displayToastUp(context,R.string.error_fetch_selectable,Toast.LENGTH_LONG)
                }
            } ?: Utils.logResponseVerbose(this::class.java, response)
        }
    }

}
