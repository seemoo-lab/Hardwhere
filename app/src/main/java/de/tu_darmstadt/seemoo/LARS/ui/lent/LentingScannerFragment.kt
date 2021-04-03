package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.editorlist.AbstractScannerFragment

/**
 * Fragment for continuous scanning assets.
 * Uses the [EditorListViewModel]
 */
class LentingScannerFragment : AbstractScannerFragment() {
    private lateinit var viewModel: LentingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LentingViewModel::class.java]
    }

    override fun addToList(asset: Asset) {
        viewModel.assetsToLent.value!!.add(0,asset)
    }

    override fun decreaseLoading() {
        viewModel.decLoading()
    }

    override fun increaseLoading() {
        viewModel.incLoading()
    }

    override fun assetList(): ArrayList<Asset> = viewModel.assetsToLent.value!!

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