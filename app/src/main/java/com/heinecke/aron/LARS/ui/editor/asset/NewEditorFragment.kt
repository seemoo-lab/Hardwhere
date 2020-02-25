package com.heinecke.aron.LARS.ui.editor.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.ui.APIFragment
import com.heinecke.aron.LARS.ui.editor.AssetTextView

class NewEditorFragment : APIFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_editor_new, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.editor_container)
        layout.addView(AssetTextView(inflater.context,null))
        return view
    }
}