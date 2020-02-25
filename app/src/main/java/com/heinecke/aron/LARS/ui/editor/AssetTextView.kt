package com.heinecke.aron.LARS.ui.editor

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.heinecke.aron.LARS.R

class AssetTextView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        View.inflate(context, R.layout.asset_text_view_text, this)

        val label: TextView = findViewById(R.id.asset_text_view_label)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AssetTextView)
        label.text = attributes.getText(R.styleable.AssetTextView_label)
        attributes.recycle()
    }
}