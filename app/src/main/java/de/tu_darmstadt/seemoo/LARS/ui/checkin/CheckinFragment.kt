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

class CheckinFragment : APIFragment(), CheckinRecyclerViewAdapter.OnListInteractionListener {

    private lateinit var recyclerViewAdapter: CheckinRecyclerViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var checkinViewModel: CheckinViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CheckinRecyclerViewAdapter
    private lateinit var scanButton: FloatingActionButton

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

        checkinViewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it ) View.VISIBLE else View.GONE
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
                Log.d(this::class.java.name,"List update ${it.size}");
            }
        })

        checkinViewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run { Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_LONG
            ).show() }
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
        //TODO("Not yet implemented")
    }

    override fun onSelectionModeChange(selectionMode: Boolean) {
        if (selectionMode) {
            // TODO
        } else {
            // TODO
        }
    }
}