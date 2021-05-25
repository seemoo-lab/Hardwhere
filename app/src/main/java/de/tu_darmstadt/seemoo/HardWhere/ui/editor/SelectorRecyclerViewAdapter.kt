package de.tu_darmstadt.seemoo.HardWhere.ui.editor


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable
import de.tu_darmstadt.seemoo.HardWhere.databinding.FragmentSelectorBinding
import de.tu_darmstadt.seemoo.HardWhere.ui.editor.SelectorRecyclerViewAdapter.OnListFragmentInteractionListener

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class SelectorRecyclerViewAdapter(
    private val mListener: OnListFragmentInteractionListener?,
    private val mValues: MutableList<Selectable> = mutableListOf()
) : RecyclerView.Adapter<SelectorRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Selectable
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListItemClicked(item)
        }
    }

    /**
     * Replace elements with new ones
     */
    fun replaceElements(newData: List<Selectable>) {
        this.mValues.clear()
        this.mValues.addAll(newData)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentSelectorBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mContentView.text = item.name

        with(holder.binding.root) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val binding: FragmentSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        val mContentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    /**
     * Interface for recyclerview listeners
     */
    interface OnListFragmentInteractionListener {
        fun onListItemClicked(item: Selectable)
    }
}
