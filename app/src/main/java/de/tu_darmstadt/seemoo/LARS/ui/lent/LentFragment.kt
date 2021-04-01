package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.os.Bundle
import android.util.Log
import android.view.*
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
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment

class LentFragment : APIFragment(), LentRecyclerViewAdapter.OnListInteractionListener {

    private lateinit var selectorViewModel: SelectorViewModel
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var lentViewModel: LentViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: LentRecyclerViewAdapter
    private lateinit var scanButton: FloatingActionButton
    private var mActionMode: ActionMode? = null
    private val CODE_SELECT_USER: Int = 10001

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
        scanButton = root.findViewById(R.id.frag_lent_scanButton)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: use refresh-layout for pulldown refresh
        // TODO: decide if we just display old data on loading failure
        lentViewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewAdapter = LentRecyclerViewAdapter(this, lentViewModel.checkedOutAsset.value!!)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.lent_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        scanButton.setOnClickListener {
            displayUserSelection()
        }

        lentViewModel.checkedOutAsset.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.notifyDataSetChanged()
                Log.d(this::class.java.name, "List update ${it.size}");
            }
        })

        lentViewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            it?.run {
                when (it.inputID) {
                    CODE_SELECT_USER -> lentViewModel.lastSelectedUser.value = it.item as Selectable.User
                    else -> Log.w(
                        this@LentFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
                selectorViewModel.resetSelected()
            }
        })

        lentViewModel.loadData(getAPI())
    }

    private fun displayUserSelection() {
        val (id, args) = SelectorFragment.newInstancePair(
            lentViewModel.lastSelectedUser.value,
            CODE_SELECT_USER,
            Selectable.SelectableType.User
        )
        findNavController().navigate(id, args)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.checkin_base, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.checkin_clear -> {
                // TODO
                true
            }
            R.id.checkin_filter -> {
                // TODO
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

    override fun onSelectionModeChange(selectionMode: Boolean) {
        if (selectionMode) {
            mActionMode = requireActivity().startActionMode(CheckinActionMode())
        } else {
            mActionMode?.finish()
        }
    }
    inner class CheckinActionMode : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.checkin_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.checkin_do_checkin -> {
                    // retrieve selected items and delete them out
                    // TODO: checkin items
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // remove selection
            viewAdapter.clearSelection()
            mActionMode = null
        }

    }
}