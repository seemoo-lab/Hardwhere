package de.tu_darmstadt.seemoo.HardWhere.ui.lent

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.ui.lib.RecyclerItemTouchHelper

class LentingRecyclerViewAdapter(
    private val mListener: OnListInteractionListener?,
    private val assetList: ArrayList<Asset>
)         :
    RecyclerView.Adapter<LentingRecyclerViewAdapter.ViewHolder>() {

    // holder class to hold reference
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), RecyclerItemTouchHelper.SwipeViewHolder {
        //get view reference
        var modelName: TextView = view.findViewById(R.id.modelName)
        var assetTag: TextView = view.findViewById(R.id.assetTag)
        var checkoutStatus: TextView = view.findViewById(R.id.checkoutStatus)
        var viewForeground: ConstraintLayout = view.findViewById(R.id.view_foreground)
        // TODO: remove checkbox, not used at all ?
        var checkbox: CheckBox = view.findViewById(R.id.recycler_checkBox)
        override fun viewForeground(): View = viewForeground
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_checkout_item_asset,
                parent,
                false
            )
        )
    }

    fun getItemAt(id: Int): Asset {
        return assetList[id]
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val asset = assetList[position]
        holder.modelName.text = asset.model?.name ?: "<no model>"
        holder.assetTag.text = asset.asset_tag ?: "<no tag>"
        holder.checkoutStatus.text = asset.assigned_to?.name ?: "<no assigner!>"
        holder.checkbox.isChecked = asset.selected
        holder.checkbox.visibility = View.INVISIBLE

        with(holder.view) {
            tag = asset
            setOnClickListener {
                mListener?.onListItemClicked(asset)
            }
        }
    }

    /**
     * Replace elements with new ones
     */
    fun replaceElements(newData: List<Asset>) {
        this.assetList.clear()
        this.assetList.addAll(newData)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return assetList.size
    }

    // update your data
    fun updateData(scanResult: ArrayList<Asset>) {
        assetList.clear()
        notifyDataSetChanged()
        assetList.addAll(scanResult)
        notifyDataSetChanged()
    }

    // prepend data to start
    fun prepend(item: Asset) {
        this.assetList.add(0, item)
        this.notifyItemInserted(0)
    }

    fun clearItems() {
        this.assetList.clear()
        this.notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        this.assetList.removeAt(position)
        this.notifyItemRemoved(position)
    }

    /**
     * Interface for recyclerview listeners
     */
    interface OnListInteractionListener {
        fun onListItemClicked(item: Asset)
    }
}