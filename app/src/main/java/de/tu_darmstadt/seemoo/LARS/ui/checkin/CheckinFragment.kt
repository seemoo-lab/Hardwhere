package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment

class CheckinFragment : APIFragment(), CheckinRecyclerViewAdapter.OnListInteractionListener {

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var checkinViewModel: CheckinViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CheckinRecyclerViewAdapter
    private lateinit var scanButton: FloatingActionButton
    private var mActionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkinViewModel =
            ViewModelProvider(requireActivity())[CheckinViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_checkout, container, false)
        progressBar = root.findViewById(R.id.progressLoading)
        progressBar.isIndeterminate = true
        scanButton = root.findViewById(R.id.frag_checkin_scanButton)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: use refresh-layout for pulldown refresh
        // TODO: decide if we just display old data on loading failure
        checkinViewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewAdapter = CheckinRecyclerViewAdapter(this, checkinViewModel.checkedOutAsset.value!!)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.checkedout_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        scanButton.setOnClickListener {
            // TODO
        }

        checkinViewModel.checkedOutAsset.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.notifyDataSetChanged()
                Log.d(this::class.java.name, "List update ${it.size}");
            }
        })

        checkinViewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        checkinViewModel.loadData(getAPI())
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