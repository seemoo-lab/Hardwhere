package de.tu_darmstadt.seemoo.HardWhere.ui.editor

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
import com.google.gson.JsonElement
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.Utils.Companion.DEFAULT_LOAD_AMOUNT
import de.tu_darmstadt.seemoo.HardWhere.data.model.SearchResults
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable
import de.tu_darmstadt.seemoo.HardWhere.ui.APIFragment
import org.acra.ACRA
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A fragment representing a list of Scannable-Items that can be searched
 *
 * This is similar to [de.tu_darmstadt.seemoo.HardWhere.ui.editorlist.AssetSearchFragment] but different enough to be a re-implementation
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_TYPE,selectType.getTypeName())
        outState.putParcelableArrayList(S_DATA,viewModel.data.value!!)
        outState.putString(S_SEARCH_STRING+selectType.getTypeName(),viewModel.searchString.value)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        savedInstanceState?.run {
            if(getString(ARG_TYPE) == selectType.getTypeName())
                viewModel.data.value!!.addAll(
                    this.getParcelableArrayList(
                        S_DATA
                    )!!
                )
            viewModel.searchString.value = getString(S_SEARCH_STRING+selectType.getTypeName(),"")
        }

        adapter = SelectorRecyclerViewAdapter(this@SelectorFragment)
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshContainer)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SelectorFragment.adapter
        }


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
        viewModel.cancelNetworkCall()
        val api = getAPI()
        viewModel.incLoading()
        val call = if (data != null && data.isNotBlank()) {
            api.searchSelectable(selectType.getTypeName(), data)
        } else {
            api.getSelectablePage(selectType.getTypeName(), DEFAULT_LOAD_AMOUNT, 0)
        }

        viewModel.setNetworkCall(call)
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
        const val S_DATA = "data"
        const val S_SEARCH_STRING: String = "search_input"

        /**
         * Get new pair of fragment id,args for spawn on NavController
         *
         * selectable stands for the preselected Selectable
         *
         * returnCode used for change events
         *
         * type is type of selectable for this returnCode and selection
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

    override fun onListItemClicked(item: Selectable) {
        Log.d(this@SelectorFragment::class.java.name, "Selected: $item")
        viewModel.setSelected(SelectorData(item, returnCode))
        Utils.hideKeyboard(requireActivity())
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
                Utils.displayToastUp(context,R.string.error_fetch_selectable,Toast.LENGTH_SHORT)
                t?.apply { ACRA.errorReporter.handleException(this) }
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
                    Utils.displayToastUp(context,R.string.error_fetch_selectable,Toast.LENGTH_SHORT)
                }
            } ?: Utils.logResponseVerbose(this::class.java, response)
        }
    }
}
