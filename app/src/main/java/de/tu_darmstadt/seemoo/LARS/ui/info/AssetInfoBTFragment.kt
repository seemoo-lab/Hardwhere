package de.tu_darmstadt.seemoo.LARS.ui.info

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.editor.AssetAttributeView

class AssetInfoBTFragment : BottomSheetDialogFragment() {
    private lateinit var infoBTViewModel: InfoBTViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoBTViewModel =
            ViewModelProvider(requireActivity())[InfoBTViewModel::class.java]
        if (savedInstanceState == null) {
            requireArguments().run {
                infoBTViewModel.infoAsset.value = this.getParcelable(PARAM_ASSET)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_asset_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location: AssetAttributeView = view.findViewById(R.id.locationPicker)
        val model: AssetAttributeView = view.findViewById(R.id.modelPicker)
        val category: EditText = view.findViewById(R.id.categoryPicker)
        val commentET: AssetAttributeView = view.findViewById(R.id.commentEditor)
        val nameET: AssetAttributeView = view.findViewById(R.id.assetName)
        val tagET: AssetAttributeView = view.findViewById(R.id.assetTag)

        val layout: LinearLayout = view.findViewById(R.id.frag_asset_info_linear_layout)



        savedInstanceState?.run {
            infoBTViewModel.restoreViewModelState(this)
        }

        infoBTViewModel.infoAsset.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setText(this.rtd_location?.name)
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                commentET.setText(this.notes)
                tagET.setText(this.asset_tag)
                nameET.setText(this.name)
                setupCustomFields(this,layout)
            }
        })
    }

    private fun setupCustomFields(asset: Asset, layout: LinearLayout) {
        Log.d(this::class.java.name,"setupCustomFields $asset")
        val fields = asset.custom_fields ?: return
        for (attrib in fields) {
            Log.d(this::class.java.name,"setting up ${attrib.value.field}")
            val view = AssetAttributeView(requireContext())
            layout.addView(view)
            view.tag = attrib.value.field
            view.setText(attrib.value.value)
            view.setLabel(attrib.key)
            view.disable()
            view.setDefaultText(attrib.value.value)
//                setupTextfield(view,{t -> run{
//                    if (editorViewModel.asset.value?.custom_fields == null) {
//                        editorViewModel.asset.value?.custom_fields = HashMap()
//                    }
//                    editorViewModel.asset.value?.custom_fields?.put(attrib.key,attrib.value.copy(value = t))
//                }
//                }) {
//                        a,o ->
//                    o.custom_fields!!.get(attrib.key)!!.let { a.custom_fields!!.put(attrib.key, it) }
//                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        infoBTViewModel.saveViewModelState(outState)
    }

    companion object {
        fun newInstance(asset: Asset): AssetInfoBTFragment {
            val bundle = Bundle()
            val fragment = AssetInfoBTFragment()
            bundle.putParcelable(PARAM_ASSET,asset)
            fragment.arguments = bundle
            return fragment
        }

        private const val PARAM_ASSET = "asset"
    }
}