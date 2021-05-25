package de.tu_darmstadt.seemoo.HardWhere.ui.myassets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.ui.info.AssetInfoBTFragment

class MyAssetClickActionBTFragment: BottomSheetDialogFragment() {
    private lateinit var asset: Asset
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            requireArguments().run {
                asset = this.getParcelable(PARAM_ASSET)
            }
        } else {
            asset = savedInstanceState.getParcelable(PARAM_ASSET)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PARAM_ASSET,asset)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_myasset_action, container, false)
        val btnFilter: Button = view.findViewById(R.id.frag_myasset_action_filter_button)
        val btnInfo: Button = view.findViewById(R.id.frag_myasset_action_info_button)

        btnFilter.setOnClickListener {
            asset?.run {
                FilterListBTFragment.newInstance(this).show(
                    parentFragmentManager,
                    "MyAssetFilterBTFragment"
                )
            }
            dismiss()
        }

        btnInfo.setOnClickListener {
            asset?.run {
                AssetInfoBTFragment.newInstance(this).show(
                    parentFragmentManager,
                    "MyAssetInfoBTFragment"
                )
            }
            dismiss()
        }

        return view
    }

    companion object {
        fun newInstance(asset: Asset): MyAssetClickActionBTFragment {
            val bundle = Bundle()
            val fragment = MyAssetClickActionBTFragment()
            bundle.putParcelable(PARAM_ASSET,asset)
            fragment.arguments = bundle
            return fragment
        }

        private const val PARAM_ASSET = "asset"
    }
}