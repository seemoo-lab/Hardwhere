package com.heinecke.aron.LARS.ui.scan

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.Utils.Companion.ITEM_OLDAGE_MS
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.ui.APIFragment
import com.heinecke.aron.LARS.ui.editor.asset.EditorFragment
import com.heinecke.aron.LARS.ui.editor.asset.EditorViewModel
import com.heinecke.aron.LARS.ui.lib.RecyclerItemTouchHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Asset list fragment for adding,scanning, removing and editing assets<br>
 *     Uses the [ScanViewModel]
 */
class AssetListFragment : APIFragment(), AssetRecyclerViewAdapter.OnListInteractionListener,
    RecyclerItemTouchHelper.SwipeListener {

    private lateinit var scanViewModel: ScanViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AssetRecyclerViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var editorViewModel: EditorViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var scanHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanViewModel =
            ViewModelProviders.of(requireActivity())[ScanViewModel::class.java]
        editorViewModel =
            ViewModelProviders.of(requireActivity())[EditorViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        if(System.currentTimeMillis() - scanViewModel.lastUpdate > ITEM_OLDAGE_MS) {
            scanViewModel.lastUpdate = System.currentTimeMillis()
            updateAssets()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(S_SCAN_LIST, scanViewModel.scanList.value)
        outState.putLong(S_UPDATE_TIME,scanViewModel.lastUpdate)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.scan, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                val (id, args) = EditorFragment.newInstancePair(
                    scanViewModel.scanList.value!!
                )
                findNavController().navigate(id, args)
                true
            }
            R.id.clear -> {
                scanViewModel.scanList.value!!.clear()
                viewAdapter.notifyDataSetChanged()
                updateHint()
                true
            }
            R.id.manual -> {
                findNavController().navigate(AssetSearchFragment.newInstanceID())
                true
            }
            else -> false
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanHint = view.findViewById(R.id.scan_hint)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { _ ->
            val id = ScannerFragment.newInstance()
            findNavController().navigate(id)
        }
        progressBar = view.findViewById(R.id.progressScanning)
        progressBar.isIndeterminate = true

        viewManager = LinearLayoutManager(context)
        savedInstanceState?.run {
            val scanList = scanViewModel.scanList.value!!
            if(scanList.size == 0) {
                scanList.addAll(this.getParcelableArrayList(S_SCAN_LIST)!!)
                scanViewModel.lastUpdate = getLong(S_UPDATE_TIME)
            }
        }
        viewAdapter = AssetRecyclerViewAdapter(this, scanViewModel.scanList.value!!)

        recyclerView = view.findViewById<RecyclerView>(R.id.frag_scan_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
            RecyclerItemTouchHelper(this, ItemTouchHelper.LEFT)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        scanViewModel.resolving.observe(this, Observer {
            progressBar.visibility = if (it > 0) View.VISIBLE else View.GONE
            updateHint()
        })

        updateHint()

        mainViewModel.scanData.observe(viewLifecycleOwner, Observer {
            it?.run {
                Log.d(this@AssetListFragment::class.java.name, "ScanData updated.")
                viewAdapter.notifyDataSetChanged()
                updateHint()
            }
        })

        // react to updates when editor finished
        editorViewModel.editingFinished.observe(this, Observer {
            if (it != null) {
                Log.d(this@AssetListFragment::class.java.name, "EditedAssets")
                updateAssets()
                editorViewModel.reset()
            }
        })
    }

    private fun updateHint() {
        scanHint.visibility =
            if (scanViewModel.scanList.value!!.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateAssets() {
        val client = getAPI()
        scanViewModel.incLoading()
        val requests: List<Observable<Asset>> = scanViewModel.scanList.value!!.map {
            client.getAssetObservable(it.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val assets = list.filter {
                if (it is Asset) {
                    return@filter true
                }
                false
            }.map { it as Asset }
            assets
        }
            .subscribe({
                scanViewModel.run {
                    decLoading()
                    Log.d(this@AssetListFragment::class.java.name, "Finished with $it")
                    scanList.value!!.clear()
                    scanList.value!!.addAll(it)
                    viewAdapter.notifyDataSetChanged()
                }
            }) {
                scanViewModel.decLoading()
                Utils.displayToastUp(context!!,R.string.error_fetch_update,Toast.LENGTH_LONG)
                Log.w(this@AssetListFragment::class.java.name, "Error: $it")
            }
    }

    companion object {
        const val S_SCAN_LIST: String = "scan_list"
        const val S_UPDATE_TIME: String = "update_time"
    }

    override fun onListItemClicked(item: Asset) {
        Toast.makeText(context, "TODO: Info",Toast.LENGTH_SHORT).show()
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        viewAdapter.removeItem(position)
    }
}
