package com.heinecke.aron.LARS.ui.scan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.ui.lib.RecyclerItemTouchHelper

class AssetRecyclerViewAdapter(
    private val mListener: OnListInteractionListener?,
    private val assetList: ArrayList<Asset>
) :
    RecyclerView.Adapter<AssetRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Asset
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListItemClicked(item)
        }
    }

    // holder class to hold reference
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), RecyclerItemTouchHelper.SwipeViewHolder {
        //get view reference
        var modelName: TextView = view.findViewById(R.id.modelName)
        var locationName: TextView = view.findViewById(R.id.locationName)
        var assetName: TextView = view.findViewById(R.id.assetName)
        var assetTag: TextView = view.findViewById(R.id.assetTag)
        var viewForeground: LinearLayout = view.findViewById(R.id.view_foreground)
        var viewBackground: RelativeLayout = view.findViewById(R.id.view_background)
        override fun viewForeground(): View = viewForeground
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_list_item_asset,
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
        //set values
        val asset = assetList[position]
        holder.modelName.text = asset.model?.name ?: "<no model>"
        holder.assetName.text = asset.name ?: "<no name>"
        holder.locationName.text = asset.rtd_location?.name ?: "<no location>"
        holder.assetTag.text = asset.asset_tag ?: "<no tag>"

        with(holder.view) {
            tag = asset
            setOnClickListener(mOnClickListener)
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

    /**
     * Interface for recyclerview listeners
     */
    interface OnListInteractionListener {
        fun onListItemClicked(item: Asset)
//        fun onListItemSwiped(item: Asset)
    }
}