package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorViewModel

/**
 * Fragment of currently lent assets through user
 */
class LentFragment : APIFragment(), LentRecyclerViewAdapter.OnListInteractionListener {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var selectorViewModel: SelectorViewModel
    private lateinit var lentViewModel: LentViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: LentRecyclerViewAdapter
    private lateinit var lentButton: FloatingActionButton
    private lateinit var filterCancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lentViewModel =
            ViewModelProvider(requireActivity())[LentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_lent_assets, container, false)
        progressBar = root.findViewById(R.id.progressLoading)
        progressBar.isIndeterminate = true
        lentButton = root.findViewById(R.id.frag_lent_lentButton)
        filterCancelButton = root.findViewById(R.id.frag_lent_stop_filter_button)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: use refresh-layout for pulldown refresh
        // TODO: decide if we just display old data on loading failure
        lentViewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewAdapter = LentRecyclerViewAdapter(this)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.lent_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            it?.run {
                when (it.inputID) {
                    R.id.lent_filter -> lentViewModel.filteredUser.value = it.item as Selectable.User
                    else -> Log.w(
                        this@LentFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
                selectorViewModel.resetSelected()
            }
        })

        lentButton.setOnClickListener {
            findNavController().navigate(LentingScanListFragment.instanceId())
        }

        lentViewModel.checkedOutAsset.observe(viewLifecycleOwner, Observer {
            it?.run {
                recalcFilter(this)
                Log.d(this::class.java.name, "List update ${it.size}");
            }
        })

        lentViewModel.filteredUser.observe(viewLifecycleOwner, Observer {
            lentViewModel.checkedOutAsset.value?.run {
                recalcFilter(this)
            }
            filterCancelButton.visibility = if (it == null) View.GONE else View.VISIBLE
            it?.run {
                filterCancelButton.text = getString(R.string.filtering_by_x,it.name)
            }
        })

        filterCancelButton.setOnClickListener {
            lentViewModel.filteredUser.value = null
        }

        lentViewModel.filteredAssets.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.replaceElements(this)
            }
        })

        lentViewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                lentViewModel.resetError()
            }
        })

        lentViewModel.loadData(getAPI(),getUserID())
    }

    fun recalcFilter(arrayList: ArrayList<Asset>) {
        val filterUser = lentViewModel.filteredUser.value
        if (filterUser == null) {
            lentViewModel.filteredAssets.value = arrayList
        } else {
            lentViewModel.filteredAssets.value = arrayList.filter { asset ->
                if (asset.assigned_to != null) {
                    asset.assigned_to.id == filterUser.id
                } else {
                    Log.w(this::class.java.name, "Asset without assigned user! ${asset.asset_tag}")
                    true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.lent, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.lent_filter -> {
                val (id, args) = SelectorFragment.newInstancePair(
                    lentViewModel.filteredUser.value,
                    R.id.lent_filter,
                    Selectable.SelectableType.User
                )
                findNavController().navigate(id, args)
                true
            }
            else -> false
        }
    }

    override fun onListItemClicked(item: Asset) {
        LentClickActionBTFragment.newInstance(item).show(
            parentFragmentManager,
            "LentAssetActionBTFragment"
        )
    }
}