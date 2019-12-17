package com.heinecke.aron.LARS.ui.editor

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.MainViewModel
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.data.APIClient
import com.heinecke.aron.LARS.data.APIInterface
import com.heinecke.aron.LARS.data.model.SearchResults
import com.heinecke.aron.LARS.data.model.Selectable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SelectorFragment.OnListFragmentInteractionListener] interface.
 */
class SelectorFragment : Fragment(),
    SelectorRecyclerViewAdapter.OnListFragmentInteractionListener,
    SearchView.OnQueryTextListener {

    // currently selected item or null
    private var selectable: Selectable? = null
    private lateinit var selectType: Selectable.SelectableType
    private lateinit var viewModel: SelectorViewModel
    private lateinit var mainModel: MainViewModel
    private lateinit var adapter: SelectorRecyclerViewAdapter
    private var returnCode: Int = 0
    private var api: APIInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireParentFragment())[SelectorViewModel::class.java]
        arguments?.let {
            selectable = it.getParcelable(ARG_SELECTABLE)
            returnCode = it.getInt(ARG_RETURN_CODE)
            selectType = it.getParcelable(ARG_TYPE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_list, container, false)
        setHasOptionsMenu(true)
        // Set the adapter
        adapter = SelectorRecyclerViewAdapter(this@SelectorFragment)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@SelectorFragment.adapter
            }
        }
        return view
    }

    private fun getAPI(): APIInterface {
        if (api == null) {
            val loginData = mainModel.getLoginData(requireContext())
            val client = APIClient.getClient(loginData.apiBackend, loginData.apiToken)
            api = client.create(APIInterface::class.java)
        }
        return api!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainModel = ViewModelProviders.of(requireActivity())[MainViewModel::class.java]

        viewModel.searchString.observe(this, Observer {

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select, menu)
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setOnQueryTextListener(this@SelectorFragment)
            setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
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
        viewModel.setSelected(SelectorData(item, returnCode))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        //TODO
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val api = getAPI()
        if (newText != null && newText.isNotEmpty()) {
            selectType.searchSelectable(newText, api).enqueue(object : Callback<SearchResults> {
                override fun onFailure(call: Call<SearchResults>?, t: Throwable?) {
                    Log.w(this@SelectorFragment::class.java.name, "$t")
                }

                override fun onResponse(
                    call: Call<SearchResults>?,
                    response: Response<SearchResults>?
                ) {
                    response?.run {
                        val elements =
                            this.body().rows.map { elem -> selectType.parseElement(elem) }
                        if (this.isSuccessful) {
                            Log.d(this@SelectorFragment::class.java.name, "Body: $elements")
                            adapter.replaceElements(elements)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Unable to fetch results",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } ?: Utils.logResponseVerbose(this@SelectorFragment::class.java, response)
                }
            })
        } else {
            Log.d(this@SelectorFragment::class.java.name, "Null search text or empty")
            //TODO
        }
        return true
    }
}
