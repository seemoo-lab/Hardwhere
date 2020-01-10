package com.heinecke.aron.LARS.ui.editor

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.JsonElement
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.Utils.Companion.DEFAULT_LOAD_AMOUNT
import com.heinecke.aron.LARS.data.model.SearchResults
import com.heinecke.aron.LARS.data.model.Selectable
import com.heinecke.aron.LARS.ui.APIFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A fragment representing a list of Scannable-Items that can be searched
 *
 * This is similar to [com.heinecke.aron.LARS.ui.scan.AssetSearchFragment] but different enough to be a re-implementation
 */
class SelectorFragment : APIFragment(),
    SelectorRecyclerViewAdapter.OnListFragmentInteractionListener,
    SearchView.OnQueryTextListener {

    // currently selected item or null
    private var selectable: Selectable? = null
    private lateinit var selectType: Selectable.SelectableType
    private lateinit var viewModel: SelectorViewModel
    private lateinit var adapter: SelectorRecyclerViewAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var returnCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectable = it.getParcelable(ARG_SELECTABLE)
            returnCode = it.getInt(ARG_RETURN_CODE)
            selectType = it.getParcelable(ARG_TYPE)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SelectorRecyclerViewAdapter(this@SelectorFragment)
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshContainer)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SelectorFragment.adapter
        }

        viewModel = ViewModelProviders.of(requireActivity())[SelectorViewModel::class.java]
        viewModel.run {
            swipeRefreshLayout.setOnRefreshListener { updateData(searchString.value) }
            searchString.observe(viewLifecycleOwner, Observer {
                updateData(it)
            })


            lastType.value.run {
                if (selectType != this) {
                    resetSearchString()
                }
                lastType.value = selectType
            }

            resolving.observe(viewLifecycleOwner, Observer {
                swipeRefreshLayout.isRefreshing = it != 0
            })
        }

    }

    private fun updateData(data: String?) {
        viewModel.lastNetworkCall?.cancel()
        viewModel.incLoading()
        val api = getAPI()
        val call = if (data != null && data.isNotBlank()) {
            api.searchSelectable(selectType.getTypeName(), data)
        } else {
            api.getSelectablePage(selectType.getTypeName(), DEFAULT_LOAD_AMOUNT, 0)
        }

        viewModel.lastNetworkCall = call
        call.enqueue(SearchResultCallback(requireContext(), selectType, adapter, viewModel))
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select, menu)
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            setOnQueryTextListener(this@SelectorFragment)
            setQuery(viewModel.searchString.value, false)
            isIconified = false
        }
    }

    class SelectorData(
        /** Item that is selected **/
        val item: Selectable,
        /** InputID that got passed, can be used by caller for input identification **/
        val inputID: Int
    )

    companion object {
        const val ARG_SELECTABLE = "selectable"
        const val ARG_RETURN_CODE = "returnCode"
        const val ARG_TYPE = "type"

        /**
         * Get new pair of fragment id,args for spawn on NavController
         */
        @JvmStatic
        fun newInstancePair(
            selectable: Selectable?,
            returnCode: Int,
            type: Selectable.SelectableType
        ): Pair<Int, Bundle> {
            val args = Bundle().apply {
                putParcelable(ARG_SELECTABLE, selectable)
                putInt(ARG_RETURN_CODE, returnCode)
                putParcelable(ARG_TYPE, type)
            }
            return Pair(R.id.selector_fragment, args)
        }
    }

    override fun onListFragmentInteraction(item: Selectable) {
        Log.d(this@SelectorFragment::class.java.name, "Selected: $item")
        viewModel.setSelected(SelectorData(item, returnCode))
        findNavController().popBackStack()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchString.value = query
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchString.value = newText
        return true
    }

    class SearchResultCallback(
        val context: Context,
        val selectType: Selectable.SelectableType,
        val adapter: SelectorRecyclerViewAdapter,
        val viewModel: SelectorViewModel
    ) : Callback<SearchResults<JsonElement>> {
        override fun onFailure(call: Call<SearchResults<JsonElement>>?, t: Throwable?) {
            viewModel.decLoading()
            if(!call!!.isCanceled) {
                Log.w(this::class.java.name, "$t")
                Toast.makeText(context, R.string.error_fetch_selectable, Toast.LENGTH_SHORT).show()
            } else {
                Log.w(this::class.java.name,"Canceled request")
            }
        }

        override fun onResponse(
            call: Call<SearchResults<JsonElement>>?,
            response: Response<SearchResults<JsonElement>>?
        ) {
            viewModel.decLoading()
            response?.run {
                val elements =
                    this.body()!!.rows.map { elem -> selectType.parseElement(elem) }
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
