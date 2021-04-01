package de.tu_darmstadt.seemoo.LARS.ui.lent.SelectableListAdapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.ui.lib.RecyclerItemTouchHelper

class SelectableRecyclerViewAdapter(
    private val mListener: OnListInteractionListener?,
    private val assetList: ArrayList<Asset>
)         :
    RecyclerView.Adapter<SelectableRecyclerViewAdapter.ViewHolder>() {

    private val selected: ArrayList<Asset> = ArrayList()

    fun selectionMode(): Boolean = selected.size > 0

    // holder class to hold reference
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), RecyclerItemTouchHelper.SwipeViewHolder {
        //get view reference
        var modelName: TextView = view.findViewById(R.id.modelName)
        var assetTag: TextView = view.findViewById(R.id.assetTag)
        var checkoutStatus: TextView = view.findViewById(R.id.checkoutStatus)
        var viewForeground: ConstraintLayout = view.findViewById(R.id.view_foreground)
        var checkbox: CheckBox = view.findViewById(R.id.recycler_checkBox)
        override fun viewForeground(): View = viewForeground
    }

    init {
        selected.addAll(assetList.filter { it.selected })
        mListener?.onSelectionModeChange(selectionMode())
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

    fun clearSelection() {
        for (asset in selected) {
            asset.selected = false
        }
        selected.clear()
        notifyDataSetChanged()
    }

    fun getItemAt(id: Int): Asset {
        return assetList[id]
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //cache
        val selectionMode = selectionMode()
        val asset = assetList[position]
        holder.modelName.text = asset.model?.name ?: "<no model>"
        holder.assetTag.text = asset.asset_tag ?: "<no tag>"
        holder.checkoutStatus.text = asset.assigned_to?.name ?: "<no assigner!>"
        holder.checkbox.isChecked = asset.selected
        holder.checkbox.visibility = if (selectionMode) View.VISIBLE else View.INVISIBLE

        with(holder.view) {
            tag = asset
            setOnClickListener {
                if (selectionMode) {
                    asset.selected = !asset.selected
                    if (asset.selected) {
                        selected.add(asset)
                    } else {
                        selected.remove(asset)
                    }
                    if (selectionMode()) {
                        this@SelectableRecyclerViewAdapter.notifyItemChanged(position)
                    } else {
                        notifyDataSetChanged()
                        mListener?.onSelectionModeChange(selectionMode())
                    }
                } else {
                    mListener?.onListItemClicked(asset)
                }
            }
            setOnLongClickListener {
                if (!selectionMode){
                    Log.d(this@SelectableRecyclerViewAdapter::class.java.name, "In selection mode")
                    asset.selected = true
                    selected.add(asset)
                    mListener?.onSelectionModeChange(selectionMode())
                    notifyDataSetChanged()
                }
                true
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

    fun selected(): Int = selected.size

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
        fun onSelectionModeChange(selectionMode: Boolean)
    }
}