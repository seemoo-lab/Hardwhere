package com.heinecke.aron.LARS.ui.scan

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
        var modelName: TextView = view.findViewById(R.id.modelName) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.recycler_layout_assets, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //set values
        holder.modelName.text =  assetList[position].model.name
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

    fun append(item: Asset) {
        this.assetList.add(0,item)
        this.notifyItemInserted(0)
    }
}