package de.tu_darmstadt.seemoo.LARS.ui.myassets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset

class FilterListBTFragment: BottomSheetDialogFragment() {
    private lateinit var asset: Asset
    private lateinit var viewModel: MyAssetsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        viewModel = ViewModelProvider(requireActivity())[MyAssetsViewModel::class.java]
        asset = if(savedInstanceState!= null) {
            savedInstanceState.getParcelable(PARAM_ASSET)
        } else {
            requireArguments().getParcelable(PARAM_ASSET)
        }
        val view = inflater.inflate(R.layout.fragment_asset_filter_exact, container, false)
        val layout: LinearLayout = view.findViewById(R.id.asset_filter_exact_layout)
        for(filterType in Asset.Companion.AssetExactFilter.values()) {
            layout.addView(inflateFilterView(inflater,filterType))
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PARAM_ASSET,asset)
    }

    private fun inflateFilterView(inflater: LayoutInflater, filter: Asset.Companion.AssetExactFilter): View {
        val view = inflater.inflate(R.layout.view_asset_filter, null)
        val text = "${getString(filter.value)} (${filter.getNamedValue(asset) ?: "<no value>"})"
        view.findViewById<TextView>(R.id.filter_button).text = text
        filter.getValue(asset)?.run {
            view.setOnClickListener {
                filter.filterValue = this
                filter.filterValueName = filter.getNamedValue(asset) ?: "<no name>"
                viewModel.filterMode.value = filter
                dismiss()
            }
        }

        return view
    }

    companion object {
        fun newInstance(asset: Asset): FilterListBTFragment {
            val bundle = Bundle()
            val fragment = FilterListBTFragment()
            bundle.putParcelable(PARAM_ASSET,asset)
            fragment.arguments = bundle
            return fragment
        }

        private const val PARAM_ASSET = "asset"
    }
}