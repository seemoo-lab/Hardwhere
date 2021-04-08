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
        if (asset.assigned_to != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.scanned_asset_checkedout,asset.asset_tag,asset.assigned_to.username),
                Toast.LENGTH_LONG
            )
                .show()
            return
        } else {
            // TODO: checkout & add to list
        }
    }

    override fun decreaseLoading() {
        viewModel.decLoading()
    }

    override fun increaseLoading() {
        viewModel.incLoading()
    }

    override fun assetList(): ArrayList<Asset> = viewModel.checkedOutAsset.value!!

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