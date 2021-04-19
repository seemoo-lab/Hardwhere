package de.tu_darmstadt.seemoo.LARS.ui.myassets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset

class SortListBTFragment: BottomSheetDialogFragment() {
    private lateinit var viewModel: MyAssetsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity())[MyAssetsViewModel::class.java]
        val view = inflater.inflate(R.layout.fragment_asset_sort, container, false)
        val layout: LinearLayout = view.findViewById(R.id.asset_sort_layout)
        for(sortType in Asset.Companion.AssetSorter.values()) {
            layout.addView(inflateFilterView(inflater,sortType))
        }
        return view
    }

    private fun inflateFilterView(inflater: LayoutInflater, sorter: Asset.Companion.AssetSorter): View {
        val view = inflater.inflate(R.layout.view_asset_filter, null)
        view.findViewById<TextView>(R.id.filter_button).text = getString(sorter.value)
        view.setOnClickListener {
            viewModel.sortMode.value = sorter
            dismiss()
        }

        return view
    }

    companion object {
        fun newInstance(): SortListBTFragment {
            return SortListBTFragment()
        }
    }
}