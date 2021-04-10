package de.tu_darmstadt.seemoo.LARS.ui.lib

import android.os.Bundle
import android.view.View
import android.widget.Toast
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment

abstract class ScanListFragment<T: ScanListViewModel>: APIFragment() {
    /**
     * Has to be initiated by implementing class!
     */
    internal lateinit var viewModel: T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updatedAssets.observe(viewLifecycleOwner, {
            it?.run {
                viewModel.assetList.value!!.clear()
                viewModel.assetList.value!!.addAll(this)
                notifyDataSetChanged()
                viewModel.resetUpdatedAssets()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            it?.run { Utils.displayToastUp(
                requireContext(),
                getString(R.string.error_fetch_update, this),
                Toast.LENGTH_LONG
            )}
            viewModel.resetError()
        })
    }

    abstract fun notifyDataSetChanged()
}