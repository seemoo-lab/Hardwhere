package de.tu_darmstadt.seemoo.LARS.ui.myassets

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
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment

/**
 * Fragment of currently lent assets through user
 */
class MyAssetsFragment : APIFragment(), MyRecyclerViewAdapter.OnListInteractionListener {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: MyAssetsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyRecyclerViewAdapter
    private lateinit var lentButton: FloatingActionButton
    private lateinit var cancelFilterBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity())[MyAssetsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_my_assets, container, false)
        progressBar = root.findViewById(R.id.frag_myassets_progressLoading)
        progressBar.isIndeterminate = true
        lentButton = root.findViewById(R.id.frag_myassets_lentButton)
        cancelFilterBtn = root.findViewById(R.id.frag_myassets_stop_filter_button)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: use refresh-layout for pulldown refresh
        // TODO: decide if we just display old data on loading failure
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewAdapter = MyRecyclerViewAdapter(this)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.frag_myassets_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        lentButton.setOnClickListener {
            findNavController().navigate(MyCheckoutScanListFragment.instanceId())
        }

        viewModel.checkedOutAsset.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.replaceElements(this)
            }
            updateCancelButton()
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetError()
            }
        })

        cancelFilterBtn.setOnClickListener {
            viewModel.filterMode.value = Asset.Companion.AssetExactFilter.None
        }

        viewModel.loadData(getAPI(),getUserID())
    }

    fun updateCancelButton() {
        val filter = viewModel.filterMode.value!!
        cancelFilterBtn.visibility = if(filter != Asset.Companion.AssetExactFilter.None) {
            val text = "${getString(filter.value)} ${filter.filterValueName}"
            cancelFilterBtn.text = text
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_assets, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.myassets_sort -> {

                true
            }
            else -> false
        }
    }

    override fun onListItemClicked(item: Asset) {
        MyAssetClickActionBTFragment.newInstance(item).show(
            parentFragmentManager,
            "MyAssetsClickActionBTFragment"
        )
    }
}