package de.tu_darmstadt.seemoo.LARS.ui.user

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorViewModel
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
    private lateinit var clearUser: MenuItem
    private lateinit var selectorViewModel: SelectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity())[AssetListViewModel::class.java]
        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
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

        viewAdapter = MyRecyclerViewAdapter(this)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.frag_user_asset_list_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        swipeRefreshLayout.setOnRefreshListener { viewModel.loadData(getAPI()) }

        viewModel.assetList.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.replaceElements(this)
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

        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            it?.run {
                when (it.inputID) {
                    R.id.menu_user_list_change -> viewModel._user.value = it.item as Selectable.User
                    else -> Log.w(
                        this@AssetListFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
                selectorViewModel.resetSelected()
            }
        })
    }

    private fun updateHint() {
        hint.visibility = if(viewModel.user.value == null || viewModel.assetList.value.isNullOrEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if(this::clearUser.isInitialized) {
            clearUser.isVisible = viewModel.user.value != null
        }

        hint.text = if(viewModel.user.value == null) {
            getString(R.string.select_user)
        } else {
            title(getString(R.string.showing_user,viewModel.user.value!!.name))
            getString(R.string.no_user_assets_hint)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.userassets, menu)
        clearUser = menu.findItem(R.id.menu_user_list_change)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_user_list_change  -> {
                val (id, args) = SelectorFragment.newInstancePair(
                    viewModel.user.value,
                    R.id.menu_user_list_change,
                    Selectable.SelectableType.User
                )
                findNavController().navigate(id, args)
                true
            }
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