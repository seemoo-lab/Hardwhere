package com.heinecke.aron.LARS.ui.editor

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.heinecke.aron.LARS.R


class AssetTextView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet? = null) : this(context,attrs,0)
    constructor(context: Context) : this(context,null,0)

    private var toggleSwitch: SwitchCompat
    private var label: TextView
    private var editor: TextView
    init {
        View.inflate(context, R.layout.asset_text_view_text, this)

        label = findViewById(R.id.asset_text_view_label)
        toggleSwitch = findViewById(R.id.asset_text_view_switch)
        editor = findViewById(R.id.asset_text_view_text)

//        val set = intArrayOf(
//            android.R.attr.minLines,
//            android.R.attr.lines,
//            android.R.attr.inputType
//        )
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AssetTextView)
//        val attr_android = context.obtainStyledAttributes(attrs, set)

        val hint = attributes.getText(R.styleable.AssetTextView_label)
        label.text = hint
        editor.hint = hint
        editor.setLines(attributes.getInt(R.styleable.AssetTextView_android_lines,1))
        editor.minLines = attributes.getInt(R.styleable.AssetTextView_android_minLines,1)
        editor.inputType = attributes.getInt(R.styleable.AssetTextView_android_inputType,InputType.TYPE_TEXT_FLAG_MULTI_LINE)

        setUpdate(attributes.getBoolean(R.styleable.AssetTextView_update,true))
        toggleSwitch.setOnCheckedChangeListener { _, b -> editor.isEnabled = b }
        attributes.recycle()
//        attr_android.recycle()
    }

    /**
     * Set onclickListener for editor
     */
    fun setEditorOnclickListener(listener: View.OnClickListener) {
        editor.setOnClickListener(listener)
    }
    fun isUpdate() = toggleSwitch.isChecked
    fun setUpdate(update: Boolean) {
        toggleSwitch.isChecked = update
        editor.isEnabled = update
    }
    fun getText() = editor.text.toString()
    /**
     * Returns the value for this attribute, null if disabled for updates
     */
    fun getValue() : String? {
        return if (isUpdate()) {
            getText()
        } else {
            null
        }
    }
    fun setText(text: String?) {
        editor.text = text
    }
}