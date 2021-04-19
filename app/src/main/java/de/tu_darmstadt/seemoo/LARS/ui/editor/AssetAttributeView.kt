package de.tu_darmstadt.seemoo.LARS.ui.editor

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import de.tu_darmstadt.seemoo.LARS.R
import kotlinx.android.synthetic.main.asset_attribute_view_text.view.*


class AssetAttributeView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet? = null) : this(context,attrs,0)
    constructor(context: Context) : this(context,null,0)

    // disable to prevent programmatic listener triggering
    private var listenSwitchChange = true
    private var originValue: String? = null
    private var oldColors: Int
    private var textChangedListener: ((text: String) -> Unit)? = null
    private var onEditorClickListener: View.OnClickListener? = null
    private var onSwitchChangedListener: ((checked: Boolean) -> Unit)? = null
    private var editable: Boolean = true
    init {
        LayoutInflater.from(context).inflate(R.layout.asset_attribute_view_text, this, true)
        asset_attribute_view_label.id = View.generateViewId()
        asset_attribute_view_text.id = View.generateViewId()
        asset_attribute_view_switch.id = View.generateViewId()

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AssetAttributeView)
        editable = attributes.getBoolean(R.styleable.AssetAttributeView_editable, true)

        if (editable)
            asset_attribute_view_switch.setOnCheckedChangeListener { _, isChecked ->
                if (listenSwitchChange) onSwitchChangedListener?.invoke(isChecked)
            }

        asset_attribute_view_switch.visibility = if(editable) View.VISIBLE else View.INVISIBLE

        val hint = attributes.getText(R.styleable.AssetAttributeView_label)
        this.isEnabled = attributes.getBoolean(R.styleable.AssetAttributeView_android_enabled,true)
        asset_attribute_view_label.text = hint
        oldColors = asset_attribute_view_text.currentTextColor
        asset_attribute_view_text.hint = hint
        val focusable = attributes.getInt(R.styleable.AssetAttributeView_android_focusable, View.FOCUSABLE_AUTO)
        if(attributes.hasValue(R.styleable.AssetAttributeView_android_inputType)) {
            asset_attribute_view_text.inputType = attributes.getInt(
                R.styleable.AssetAttributeView_android_inputType,
                InputType.TYPE_TEXT_VARIATION_NORMAL
            )
        }

        if (editable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                asset_attribute_view_text.focusable = focusable
                Log.d(this::class.java.name, "Focusable (${asset_attribute_view_text.focusable})")
            } else {  // try to work around old android versions
                Log.d(this::class.java.name, "Using workaround for focusable")
                // workaround:
                // can't set focusable=false in xml, can't re-enable focus, old API
                // but without we get a double-focus (two clicks for onClick to trigger)
                asset_attribute_view_text.onFocusChangeListener =
                    OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            onEditorClickListener?.run {
                                this.onClick(asset_attribute_view_text)
                            }
                        }
                    }
                asset_attribute_view_text.inputType =
                    if (focusable == View.NOT_FOCUSABLE) 0 else InputType.TYPE_CLASS_TEXT
            }
            asset_attribute_view_text.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    updateChangedState()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }
            })
        } else {
            asset_attribute_view_text.isEnabled = false
        }


        setUpdate(attributes.getBoolean(R.styleable.AssetAttributeView_update,true))
        attributes.recycle()
    }

    /**
     * Irreversible hack to allow disabling stuff for into BT without style attributes
     */
    public fun disable() {
        asset_attribute_view_text.isEnabled = false

    }

    private fun updateChangedState() {
        val text = getText().toString()
        val changed = originValue != text && !(originValue.isNullOrEmpty() && text.isEmpty())
        val color = if(changed) {
            resources.getColor(R.color.colorAccent,context.theme)
        } else {
            oldColors
        }
        asset_attribute_view_text.setTextColor(color)
        setUpdate(changed)
        textChangedListener?.invoke(text)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        asset_attribute_view_switch.isEnabled = enabled
        asset_attribute_view_text.isEnabled = enabled
        setUpdate(enabled)
    }

    /**
     * Set listener for update switch change
     */
    fun setOnCheckedChangeListener(listener: ((checked: Boolean) -> Unit)?) {
        onSwitchChangedListener = listener
    }

    fun setLabel(label: String) {
        asset_attribute_view_label.text = label
        asset_attribute_view_text.hint = label
    }

    override fun isEnabled(): Boolean {
        return super.isEnabled()
    }

    override fun onSaveInstanceState(): Parcelable? {
        // TODO: honor save-state attribute, we probably don't need to store anything here
        // due to our workaround with the navigation component
        val bundle = Bundle()
        bundle.putBoolean(S_UPDATE,isUpdate())
        bundle.putString(S_ORIGIN,originValue)
        bundle.putString(S_CURRENT,getText().toString())
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle

    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // TODO: see onSaveInstanceState
        var viewState = state
        if (viewState is Bundle) {
            setDefaultText(viewState.getString(S_ORIGIN))
            setText(viewState.getString(S_CURRENT))
            setUpdate(viewState.getBoolean(S_UPDATE))
            viewState = viewState.getParcelable("superState")
        }
        super.onRestoreInstanceState(viewState)
    }

    /**
     * Set onTextChangedListener for editor
     */
    fun setTextChangedListener(listener: (text: String) -> Unit) {
        textChangedListener = listener
    }

    /**
     * Set onclickListener for editor
     */
    fun setEditorOnclickListener(listener: View.OnClickListener) {
        onEditorClickListener = listener
        asset_attribute_view_text.setOnClickListener(listener)
    }
    fun isUpdate() = asset_attribute_view_switch.isChecked && this.isEnabled
    /**
     * Set value update state for attribute editor & switch.
     */
    private fun setUpdate(update: Boolean) {
        listenSwitchChange = false
        asset_attribute_view_switch.isChecked = update && this.isEnabled
        listenSwitchChange = true
    }
    fun getText() = asset_attribute_view_text.text
    /**
     * Returns the value for this attribute, null if disabled for updates
     */
    fun getValue() : String? {
        return if (isUpdate()) {
            getText().toString()
        } else {
            null
        }
    }

    @JvmField var S_UPDATE ="update_val"
    @JvmField var S_CURRENT ="current_val"
    @JvmField var S_ORIGIN ="origin_val"

    /**
     * Set display text, if null "" will be used an null as origin value.
     */
    fun setText(text: String?) {
        Log.d(this::class.java.name,"Setting text $text")
        asset_attribute_view_text.setText(text)
    }

    /**
     * Sets original/default text, used as comparator value for deciding if anything changed
     */
    fun setDefaultText(text: String?) {
        Log.d(this::class.java.name,"Setting default $text")
        originValue = text
    }
}