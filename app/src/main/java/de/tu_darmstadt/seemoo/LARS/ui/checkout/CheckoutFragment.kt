package de.tu_darmstadt.seemoo.LARS.ui.checkout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment

class CheckoutFragment : APIFragment(), AssetRecyclerViewAdapter.OnListInteractionListener {

    private lateinit var recyclerViewAdapter: AssetRecyclerViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var checkoutViewModel: CheckoutViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AssetRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkoutViewModel =
            ViewModelProvider(requireActivity())[CheckoutViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_checkout, container, false)
        progressBar = root.findViewById(R.id.progressLoading)
        progressBar.isIndeterminate = true
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkoutViewModel.loading.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it ) View.VISIBLE else View.GONE
        })

        viewAdapter = AssetRecyclerViewAdapter(this, checkoutViewModel.checkedOutAsset.value!!)
        viewManager = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.checkedout_recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        checkoutViewModel.checkedOutAsset.observe(viewLifecycleOwner, Observer {
            it?.run {
                viewAdapter.notifyDataSetChanged()
                Log.d(this::class.java.name,"List update ${it.size}");
            }
        })

        checkoutViewModel.error.observe(viewLifecycleOwner, Observer {
            it?.run { Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_LONG
            ).show() }
        })

        checkoutViewModel.loadData(getAPI())
    }

    override fun onListItemClicked(item: Asset) {
        TODO("Not yet implemented")
    }
}