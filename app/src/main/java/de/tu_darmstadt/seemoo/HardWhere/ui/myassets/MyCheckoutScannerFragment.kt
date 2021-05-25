package de.tu_darmstadt.seemoo.HardWhere.ui.myassets

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.ui.lib.AbstractScannerFragment

/**
 * Fragment for continuous scanning assets.
 * Uses the [EditorListViewModel]
 */
class MyCheckoutScannerFragment : AbstractScannerFragment() {
    private lateinit var viewModel: MyCheckoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MyCheckoutViewModel::class.java]
    }

    override fun addToList(asset: Asset) {
        if (asset.assigned_to != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.scanned_asset_checkedout,asset.asset_tag,asset.assigned_to.username),
                Toast.LENGTH_LONG
            )
            .show()
        }
        viewModel.assetList.value!!.add(0,asset)
    }

    override fun decreaseLoading() {
        viewModel.decLoading()
    }

    override fun increaseLoading() {
        viewModel.incLoading()
    }

    override fun assetList(): ArrayList<Asset> = viewModel.assetList.value!!

    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstance(): Int {
            return R.id.ownCheckoutScannerFragment
        }
    }
}