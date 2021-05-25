package de.tu_darmstadt.seemoo.HardWhere.ui.lib

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * VList touch helper for recyclerview.
 * Taken from vocabletrainer-android, copyright Aron Heinecke.
 * https://github.com/0xpr03/VocableTrainer-Android
 */
class RecyclerItemTouchHelper
/**
 * Create new list touch helper with specified swipe listener
 * @param swipeListener
 */(private val swipeListener: SwipeListener, swipeDirs: Int) :
    ItemTouchHelper.SimpleCallback(0, swipeDirs) {
    /**
     * Swipe listener
     */
    interface SwipeListener {
        /**
         * Called on swipe
         * @param viewHolder
         * @param position adapter position
         */
        fun onSwiped(
            viewHolder: RecyclerView.ViewHolder?,
            direction: Int,
            position: Int
        )
    }

    /**
     * Swipe viewholder that has a foreground layout
     */
    interface SwipeViewHolder {
        fun viewForeground(): View
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeListener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        if (viewHolder != null) {
            val foregroundView: View = (viewHolder as SwipeViewHolder).viewForeground()
            ItemTouchHelper.Callback.getDefaultUIUtil()
                .onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView: View = (viewHolder as SwipeViewHolder).viewForeground()
        ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        val foregroundView: View = (viewHolder as SwipeViewHolder).viewForeground()
        ItemTouchHelper.Callback.getDefaultUIUtil()
            .clearView(foregroundView)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val foregroundView: View = (viewHolder as SwipeViewHolder).viewForeground()
        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
            c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive
        )
    }

}