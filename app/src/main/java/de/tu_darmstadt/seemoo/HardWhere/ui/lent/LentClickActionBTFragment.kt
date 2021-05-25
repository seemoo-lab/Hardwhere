package de.tu_darmstadt.seemoo.HardWhere.ui.lent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.ui.info.AssetInfoBTFragment
import de.tu_darmstadt.seemoo.HardWhere.ui.user.AssetListFragment

/**
 * Asset click action BSD fragment for LentFragment
 */
class LentClickActionBTFragment: BottomSheetDialogFragment() {
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
        val viewModel: LentViewModel = ViewModelProvider(requireActivity())[LentViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_lent_asset_action, container, false)
        val btnFilter: Button = view.findViewById(R.id.frag_lent_asset_action_filter_button)
        val btnInfo: Button = view.findViewById(R.id.frag_lent_asset_action_info_button)
        val btnUserDetails: Button = view.findViewById(R.id.frag_lent_asset_action_userdetails_button)

        val userName = asset?.assigned_to?.username ?: "<missing>"
        btnFilter.text = getString(R.string.filter_by_user_x,userName)
        btnUserDetails.text = getString(R.string.show_assets_of_x,userName)

        btnFilter.setOnClickListener {
            asset?.assigned_to?.run {
                viewModel.filteredUser.value = this.asSelector()
            }
            dismiss()
        }

        btnInfo.setOnClickListener {
            asset?.run {
                AssetInfoBTFragment.newInstance(this).show(
                    parentFragmentManager,
                    "LentAssetInfoBTFragment"
                )
            }
            dismiss()
        }

        btnUserDetails.setOnClickListener {
            asset?.assigned_to?.run {
                val (id,args) = AssetListFragment.newInstancePair(this.asSelector())
                findNavController().navigate(id, args)
            }
            dismiss()
        }
        return view
    }

    companion object {
        fun newInstance(asset: Asset): LentClickActionBTFragment {
            val bundle = Bundle()
            val fragment = LentClickActionBTFragment()
            bundle.putParcelable(PARAM_ASSET,asset)
            fragment.arguments = bundle
            return fragment
        }

        private const val PARAM_ASSET = "asset"
    }
}