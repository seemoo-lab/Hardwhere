package de.tu_darmstadt.seemoo.LARS.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.editor.AssetAttributeView

class AssetInfoBTFragment : BottomSheetDialogFragment() {
    private lateinit var scanViewModel: ScanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanViewModel =
            ViewModelProvider(requireActivity())[ScanViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val viewModel: ScanViewModel = ViewModelProvider(requireActivity())[ScanViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_asset_info, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location: AssetAttributeView = view.findViewById(R.id.locationPicker)
        val model: AssetAttributeView = view.findViewById(R.id.modelPicker)
        val category: EditText = view.findViewById(R.id.categoryPicker)
        val commentET: AssetAttributeView = view.findViewById(R.id.commentEditor)
        val nameET: AssetAttributeView = view.findViewById(R.id.assetName)
        val tagET: AssetAttributeView = view.findViewById(R.id.assetTag)

        scanViewModel.infoAsset.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setText(this.rtd_location?.name)
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                commentET.setText(this.notes)
                tagET.setText(this.asset_tag)
                nameET.setText(this.name)
            }
        })
    }

    companion object {
        fun newInstance(): AssetInfoBTFragment {
            return AssetInfoBTFragment()
        }
    }
}