package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import de.tu_darmstadt.seemoo.LARS.ui.editorlist.EditorListViewModel
import de.tu_darmstadt.seemoo.LARS.ui.editorlist.EditorScannerFragment

/**
 * View for selecting assets that can then be lent to other users
 */
class LentingScanListFragment: APIFragment(), LentingRecyclerViewAdapter.OnListInteractionListener {
    private lateinit var selectorViewModel: SelectorViewModel
    private lateinit var lentingViewModel: LentingViewModel
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: LentingRecyclerViewAdapter
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
        lentingViewModel = ViewModelProvider(requireActivity())[LentingViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_lenting_asset_scanlist, container, false)
        progressBar = root.findViewById(R.id.frag_lenting_scanlist_progress)
        progressBar.isIndeterminate = true
        hintText = root.findViewById(R.id.frag_lenting_scanlist_scan_hint)
        lentButton = root.findViewById(R.id.frag_lenting_scanlist_scan_button)
        return root
    }

    private fun displayUserSelection() {
        val (id, args) = SelectorFragment.newInstancePair(
            lentingViewModel.lastSelectedUser.value,
            R.id.frag_lenting_scanlist_recycler,
            Selectable.SelectableType.User
        )
        findNavController().navigate(id, args)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.lenting, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.lentingNext -> {
                displayUserSelection()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            it?.run {
                when (it.inputID) {
                    R.id.frag_lenting_scanlist_recycler -> {
                        val item = it.item as Selectable.User
                        lentingViewModel.lastSelectedUser.value = item
                        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                        alertDialogBuilder.setMessage(getString(R.string.lenting_alert_confirm_msg,item.name))
                        alertDialogBuilder.setCancelable(true)

                        alertDialogBuilder.setPositiveButton(
                            getString(android.R.string.yes)
                        ) { dialog, _ ->
                            lentingViewModel.checkout(getAPI())
                            dialog.cancel()
                        }
                        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel)) {
                            dialog,_ -> dialog.cancel()
                        }

                        val alertDialog: AlertDialog = alertDialogBuilder.create()
                        alertDialog.show()

                    }
                    else -> Log.w(
                        this@LentingScanListFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
                selectorViewModel.resetSelected()
            }
        })

        viewAdapter = LentingRecyclerViewAdapter(this, lentingViewModel.assetsToLent.value!!)
        viewManager = LinearLayoutManager(context)


        recyclerView = view.findViewById<RecyclerView>(R.id.frag_lenting_scanlist_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        lentingViewModel.loading.observe(viewLifecycleOwner, Observer {
            it?.run {
                progressBar.visibility = if(this > 0) View.VISIBLE else View.GONE
            }
        })

        lentingViewModel.assetsToLent.observe(viewLifecycleOwner, Observer {
            hintText.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.INVISIBLE
        })

        lentButton.setOnClickListener {
            val id = LentingScannerFragment.newInstance()
            findNavController().navigate(id)
        }

        mainViewModel.scanData.observe(viewLifecycleOwner, Observer {
            it?.run {
                Log.d(this@LentingScanListFragment::class.java.name, "ScanData updated.")

                viewAdapter.notifyDataSetChanged()
            }
        })
    }

    companion object {
        /**
         * Create a new navigation resource ID and param bundle pair to be used with nagController.
         */
        @JvmStatic
        fun instanceId(): Int {
            return R.id.lentingScanListFragment
        }
    }

    override fun onListItemClicked(item: Asset) {
        // TODO("Not yet implemented")
    }
}