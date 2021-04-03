package de.tu_darmstadt.seemoo.LARS.ui.editorlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset.Companion.AssetFilter

/**
 * Asset filter options as BottomSheetDialogFragment.
 * Uses the [EditorListViewModel]
 */
class AssetFilterBTFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val viewModel: EditorListViewModel = ViewModelProvider(requireActivity())[EditorListViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_asset_filter, container, false)
        val layout: LinearLayout = view.findViewById(R.id.asset_filter_layout)
        for(filterType in AssetFilter.values()) {
            layout.addView(inflateFilterView(inflater,filterType, viewModel.filterMode))
        }
        return view
    }

    private fun inflateFilterView(inflater: LayoutInflater, filter: AssetFilter, mode: MutableLiveData<AssetFilter>): View {
        val view = inflater.inflate(R.layout.view_asset_filter, null)
        view.findViewById<TextView>(R.id.filter_button).setText(filter.value)
        view.setOnClickListener {
            mode.value = filter
            dismiss()
        }
        return view
    }
}