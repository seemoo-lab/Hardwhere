package com.heinecke.aron.LARS.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Asset.Companion.AssetFilter

/**
 * Asset filter options as BottomSheetDialogFragment
 */
class AssetFilterBTFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val viewModel: ScanViewModel = ViewModelProviders.of(requireActivity())[ScanViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_asset_filter, container, false)
        val layout: LinearLayout = view.findViewById(R.id.asset_filter_layout)
        for(filterType in AssetFilter.values()) {
            layout.addView(inflateFilterView(inflater,filterType, viewModel.filterMode))
        }
        return view
    }

    private fun inflateFilterView(inflater: LayoutInflater, filter: AssetFilter, mode: MutableLiveData<AssetFilter>): View {
        val view = inflater.inflate(R.layout.view_asset_filter, null)
//        view.tag = filter.name
        view.findViewById<TextView>(R.id.filter_button).setText(filter.value)
        view.setOnClickListener {
            mode.value = filter
            dismiss()
        }
        return view
    }
}