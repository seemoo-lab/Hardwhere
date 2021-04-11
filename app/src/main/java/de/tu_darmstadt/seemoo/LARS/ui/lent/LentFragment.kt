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
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.info.AssetInfoBTFragment

/**
 * Fragment of currently lent assets through user
 */
class LentFragment : APIFragment(), LentRecyclerViewAdapter.OnListInteractionListener {
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var lentViewModel: LentViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: LentRecyclerViewAdapter
    private lateinit var lentButton: FloatingActionButton

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

        lentButton.setOnClickListener {
            findNavController().navigate(LentingScanListFragment.instanceId())
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
                lentViewModel.resetError()
            }
        })

        lentViewModel.loadData(getAPI(),getUserID())
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.checkin_base, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.checkin_sort -> {
                // TODO
                Utils.displayTodo(requireContext())
                true
            }
            R.id.checkin_filter -> {
                // TODO
                Utils.displayTodo(requireContext())
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
        // TODO: allow also filtering by
    }
}