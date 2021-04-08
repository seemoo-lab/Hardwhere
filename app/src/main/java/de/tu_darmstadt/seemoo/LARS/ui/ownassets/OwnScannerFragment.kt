package de.tu_darmstadt.seemoo.LARS.ui.ownassets

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.editorlist.AbstractScannerFragment

/**
 * Fragment for continuous scanning assets.
 * Uses the [EditorListViewModel]
 */
class OwnScannerFragment : AbstractScannerFragment() {
    private lateinit var viewModel: OwnAssetsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OwnAssetsViewModel::class.java]
    }

    override fun addToList(asset: Asset) {
        if (asset.assigned_to == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.scanned_asset_not_checkedout,asset.asset_tag),
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
        if (asset.assigned_to.id != getUserID()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.scanned_asset_not_own,asset.asset_tag),
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
    }

    override fun decreaseLoading() {
        viewModel.decLoading()
    }

    override fun increaseLoading() {
        viewModel.incLoading()
    }

    // we don't have anything to de-duplicate, ignore
    override fun assetList(): ArrayList<Asset> = ArrayList()

    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstance(): Int {
            return R.id.lentingScannerFragment
        }
    }
}