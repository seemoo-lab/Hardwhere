package de.tu_darmstadt.seemoo.LARS.ui.user

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment
import de.tu_darmstadt.seemoo.LARS.ui.myassets.MyRecyclerViewAdapter

/**
 * Display assets of another user
 */
class AssetListFragment: APIFragment(), MyRecyclerViewAdapter.OnListInteractionListener {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: AssetListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyRecyclerViewAdapter
    private lateinit var hint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity())[AssetListViewModel::class.java]

        if (savedInstanceState == null) {
            arguments?.run {
                viewModel._user.value = this.getParcelable(PARAM_USER)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_user_asset_list, container, false)
        progressBar = root.findViewById(R.id.frag_user_asset_list_progress)
        progressBar.isIndeterminate = true
        swipeRefreshLayout = root.findViewById(R.id.frag_user_asset_list_swipeRefreshLayout)
        hint = root.findViewById(R.id.frag_user_asset_list_hint)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: use refresh-layout for pulldown refresh
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it > 0) View.VISIBLE else View.GONE
        })

        viewAdapter = MyRecyclerViewAdapter(this, viewModel.assetList.value!!)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.frag_user_asset_list_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        swipeRefreshLayout.setOnRefreshListener { viewModel.loadData(getAPI()) }

        viewModel.assetList.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.notifyDataSetChanged()
            }
            updateHint()
        })

        viewModel.error.observe(viewLifecycleOwner, {
            it?.run {
                val (id, details) = this
                val text = if(details != null) {
                    getString(id, details.message)
                } else {
                    getString(id)
                }
                Utils.displayToastUp(
                    requireContext(),
                    text,
                    Toast.LENGTH_LONG)
                viewModel.resetError()
            }
        })

        viewModel.user.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.loadData(getAPI())
            } else {
                viewModel.assetList.value!!.run {
                    val size = this.size
                    if (size > 0) {
                        this.clear()
                        viewAdapter.notifyItemRangeRemoved(0, size)
                    }
                }
            }
            updateHint()
        })
    }

    private fun updateHint() {
        hint.visibility = if(viewModel.user.value == null || viewModel.assetList.value.isNullOrEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        hint.text = if(viewModel.user.value == null) {
            getString(R.string.select_user)
        } else {
            getString(R.string.no_user_assets_hint)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.userassets, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            else -> false
        }
    }

    override fun onListItemClicked(item: Asset) {
        AssetInfoBTFragment.newInstance(item).show(
            parentFragmentManager,
            "CheckinAssetInfoBTFragment"
        )
    }

    companion object {
        /**
         * Create a new navigation resource ID and param bundle pair to be used with nagController.
         */
        @JvmStatic
        fun newInstancePair(user: Selectable.User): Pair<Int, Bundle> {
            val args = Bundle()
            args.putParcelable(PARAM_USER, user)
            return Pair(R.id.nav_user_assets, args)
        }

        private const val PARAM_USER = "user"
    }
}