package com.heinecke.aron.LARS.ui.scan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Asset

class ScanViewAdapter(private val assetList: ArrayList<Asset>) : RecyclerView.Adapter<ScanViewAdapter.ViewHolder>() {

    // holder class to hold reference
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        var modelName: TextView = view.findViewById(R.id.modelName)
        var locationName: TextView = view.findViewById(R.id.locationName)
        var assetID: TextView = view.findViewById(R.id.assetID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.fragment_scan_list, parent, false))
    }

    fun getItemAt(id: Int) : Asset {
        return assetList[id]
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //set values
        val asset = assetList[position]
        holder.modelName.text =  asset.model?.name ?: "<no model>"
        holder.assetID.text = asset.id.toString()
        holder.locationName.text = asset.rtd_location?.name ?: "<no location>"
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
        this.assetList.add(0,item)
        this.notifyItemInserted(0)
    }
}