package de.tu_darmstadt.seemoo.LARS.ui.about

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Asset

class AboutViewAdapter(
    private val mListener: OnListInteractionListener?,
    private val aboutList: MutableList<AboutFragment.About>
)         :
    RecyclerView.Adapter<AboutViewAdapter.ViewHolder>() {

    // holder class to hold reference
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        var name: TextView = view.findViewById(R.id.about_item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_about_item,
                parent,
                false
            )
        )
    }

    /**
     * Replace elements with new ones
     */
    fun replaceElements(newData: List<AboutFragment.About>) {
        this.aboutList.clear()
        this.aboutList.addAll(newData)
        this.notifyDataSetChanged()
    }

    fun getItemAt(id: Int): AboutFragment.About {
        return aboutList[id]
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = aboutList[position]
        holder.name.text = entry.name

        with(holder.view) {
            tag = entry
            setOnClickListener {
                mListener?.onListItemClicked(entry)
            }
        }
    }

    override fun getItemCount(): Int {
        return aboutList.size
    }

    fun removeItem(position: Int) {
        this.aboutList.removeAt(position)
        this.notifyItemRemoved(position)
    }

    /**
     * Interface for recyclerview listeners
     */
    interface OnListInteractionListener {
        fun onListItemClicked(item: AboutFragment.About)
    }
}