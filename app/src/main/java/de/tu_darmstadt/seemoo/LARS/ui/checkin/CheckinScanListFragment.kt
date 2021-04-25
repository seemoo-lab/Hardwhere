package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorFragment
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment
import de.tu_darmstadt.seemoo.LARS.ui.lib.RecyclerItemTouchHelper
import de.tu_darmstadt.seemoo.LARS.ui.lib.ScanListFragment

/**
 * View for selecting assets that can then be lent to other users
 */
class CheckinScanListFragment: ScanListFragment<CheckinViewModel>(), CheckinRecyclerViewAdapter.OnListInteractionListener,
    RecyclerItemTouchHelper.SwipeListener {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CheckinRecyclerViewAdapter
    private lateinit var hintText: TextView
    private lateinit var lentButton: FloatingActionButton

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CheckinViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_checkin_asset_scanlist, container, false)
        progressBar = root.findViewById(R.id.frag_checkin_scanlist_progress)
        progressBar.isIndeterminate = true
        hintText = root.findViewById(R.id.frag_checkin_scanlist_scan_hint)
        lentButton = root.findViewById(R.id.frag_checkin_scanlist_scan_button)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.lenting, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.lentingNext -> {
                val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setMessage(getString(R.string.checkin_alert_confirm_msg))
                alertDialogBuilder.setCancelable(true)

                alertDialogBuilder.setPositiveButton(
                    getString(android.R.string.yes)
                ) { dialog, _ ->
                    viewModel.checkin(getAPI())
                    dialog.cancel()
                }
                alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel)) {
                        dialog,_ -> dialog.cancel()
                }

                val alertDialog: AlertDialog = alertDialogBuilder.create()
                alertDialog.show()
                true
            }
            R.id.lenting_clear -> {
                val items = viewModel.assetList.value!!.size
                viewModel.assetList.value!!.clear()
                viewAdapter.notifyItemRangeRemoved(0,items)
                updateHint()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewAdapter = CheckinRecyclerViewAdapter(this, viewModel.assetsToReturn.value!!)
        viewManager = LinearLayoutManager(context)


        recyclerView = view.findViewById<RecyclerView>(R.id.frag_checkin_scanlist_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            it?.run {
                progressBar.visibility = if(this > 0) View.VISIBLE else View.GONE
            }
        })

        val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
            RecyclerItemTouchHelper(this, ItemTouchHelper.LEFT)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        viewModel.finishedAssets.observe(viewLifecycleOwner, Observer {
            it?.run {
                Log.d(this@CheckinScanListFragment::class.java.name,"finished assets $this")
                viewModel.assetsToReturn.value!!.removeAll(this)
                viewAdapter.notifyDataSetChanged()
                updateHint()
                viewModel.resetFinishedAssets()
            }
        })

        viewModel.assetsToReturn.observe(viewLifecycleOwner, Observer {
            updateHint()
        })

        lentButton.setOnClickListener {
            findNavController().navigate(CheckinScannerFragment.newInstance())
        }

        mainViewModel.scanData.observe(viewLifecycleOwner, Observer {
            it?.run {
                Log.d(this@CheckinScanListFragment::class.java.name, "ScanData updated.")

                viewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun updateHint() {
        hintText.visibility =
            if (viewModel.assetsToReturn.value.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        /**
         * Create a new navigation resource ID and param bundle pair to be used with nagController.
         */
        @JvmStatic
        fun instanceId(): Int {
            return R.id.ownCheckoutScanListFragment
        }
    }

    override fun onListItemClicked(item: Asset) {
        AssetInfoBTFragment.newInstance(item).show(
            parentFragmentManager,
            "CheckinAssetInfoBTFragment"
        )
    }

    override fun notifyDataSetChanged() {
        viewAdapter.notifyDataSetChanged()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        viewAdapter.removeItem(position)
        if(viewAdapter.itemCount == 0)
            updateHint()
    }
}