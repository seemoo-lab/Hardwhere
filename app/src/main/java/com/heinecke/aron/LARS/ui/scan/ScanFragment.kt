package com.heinecke.aron.LARS.ui.scan

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.ui.APIFragment
import com.heinecke.aron.LARS.ui.editor.EditorFragment
import com.heinecke.aron.LARS.ui.editor.EditorViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanFragment : APIFragment() {

    private lateinit var scanViewModel: ScanViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ScanViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var editorViewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanViewModel =
            ViewModelProviders.of(requireActivity())[ScanViewModel::class.java]
        editorViewModel =
            ViewModelProviders.of(requireActivity())[EditorViewModel::class.java]
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
        outState.putParcelableArrayList(S_SCAN_LIST,scanViewModel.scanList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.scan, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(this::class.java.name, "onOptionsItemSelected")
        return when (item.itemId) {
            R.id.edit -> {
                Log.d(this::class.java.name, "starting editor")
                // use empty item otherwise
                val asset = if (viewAdapter.itemCount == 1) {
                    viewAdapter.getItemAt(0)
                } else {
                    Asset.getEmptyAsset(true)
                }
                val (id, args) = EditorFragment.newInstancePair(asset,scanViewModel.scanList)
                findNavController().navigate(id, args)
                true
            }
            R.id.clear -> {
                scanViewModel.scanList.clear()
                viewAdapter.notifyDataSetChanged()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = view.findViewById(R.id.text_gallery)
        scanViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })


        val api = getAPI()

        viewManager = LinearLayoutManager(context)
        savedInstanceState?.run { scanViewModel.scanList.addAll(this.getParcelableArrayList(S_SCAN_LIST)!!) }
        viewAdapter = ScanViewAdapter(scanViewModel.scanList)

        recyclerView = view.findViewById<RecyclerView>(R.id.frag_scan_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        mainViewModel.scanData.observe(viewLifecycleOwner, Observer {
            it?.run {
                textView.text = "Last ID: $this"
                val id = this
                api.getAsset(this).enqueue(object : Callback<Asset> {
                    override fun onFailure(call: Call<Asset>?, t: Throwable?) {
                        Log.e(this::class.java.name, "Error resolving $id: $t")
                        Toast.makeText(requireContext(), "Can't request: $t", Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onResponse(call: Call<Asset>?, response: Response<Asset>?) {
                        response?.run {
                            if (this.isSuccessful && this.body()!!.id == id) {
                                viewAdapter.prepend(this.body()!!)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.invalid_scan_id,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } ?: Utils.logResponseVerbose(this@ScanFragment::class.java, response)
                    }

                })

            } ?: textView.setText("No ID")
        })

        // react to updates when editor finished
        editorViewModel.editingFinished.observe(this, Observer {
            if (it != null) {
                Log.d(this@ScanFragment::class.java.name, "EditedAssets")
                updateAssets()
                editorViewModel.reset()
            }
        })
    }

    private fun updateAssets() {
        val client = getAPI()
        val requests: List<Observable<Asset>> = scanViewModel.scanList.map {
            client.getAssetObservable(it.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val assets = list.filter{
                if (it is Asset) {
                    return@filter true
                }
                false
            }.map { it as Asset }
            assets
        }
            .subscribe({
                Log.d(this@ScanFragment::class.java.name,"Finished with $it")
                scanViewModel.scanList.clear()
                scanViewModel.scanList.addAll(it)
                viewAdapter.notifyDataSetChanged()
            }) {
                Log.w(this@ScanFragment::class.java.name,"Error: $it")
            }
    }

    companion object {
        const val S_SCAN_LIST: String = "scan_list"
    }
}
