package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.lib.AbstractScannerFragment

/**
 * Fragment for continuous scanning assets.
 * Uses the [EditorListViewModel]
 */
class CheckinScannerFragment : AbstractScannerFragment() {
    private lateinit var viewModel: CheckinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CheckinViewModel::class.java]
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
        viewModel.assetsToReturn.value!!.add(0,asset)
    }

    override fun decreaseLoading() {
        viewModel.decLoading()
    }

    override fun increaseLoading() {
        viewModel.incLoading()
    }

    override fun assetList(): ArrayList<Asset> = viewModel.assetsToReturn.value!!

    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstance(): Int {
            return R.id.checkinScannerFragment
        }
    }
}