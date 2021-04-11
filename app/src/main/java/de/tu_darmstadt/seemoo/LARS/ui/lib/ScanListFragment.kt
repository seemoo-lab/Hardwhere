package de.tu_darmstadt.seemoo.LARS.ui.lib

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
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
            it?.run {
                val (id, details) = this
                val text = if(details != null) {
                    getString(id, details.message)
                } else {
                    getString(id)
                }
                Utils.displayToastUp(
                requireContext(),
                text,
                Toast.LENGTH_LONG)
                viewModel.resetError()
            }
        })
    }

    /**
     * Get first position of asset in [viewModel.assetList] that is assigned
     */
    private fun getFirstAssignedPosition(): Int? {
        viewModel.assetList.value!!.run {
            for (i in 0..this.size) {
                if (this[i].assigned_to != null) {
                    return i
                }
            }
        }

        return null
    }

    /**
     * Check for assigned assets, return false if assets are assigned.
     * Displays UI information on failure.
     */
    fun <T: RecyclerView.ViewHolder> verifyNoAssignedAssets(recyclerView: RecyclerView, viewAdapter: Adapter<T>): Boolean {
        val assigned = getFirstAssignedPosition()
        if (assigned != null) {
            Toast.makeText(requireContext(),R.string.checkout_assets_assigned_msg,Toast.LENGTH_LONG).show()
            recyclerView.scrollToPosition(assigned)
            viewAdapter.notifyItemChanged(assigned)
            return false
        }
        return true
    }

    abstract fun notifyDataSetChanged()
}