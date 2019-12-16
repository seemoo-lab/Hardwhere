package com.heinecke.aron.LARS.ui.editor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.heinecke.aron.LARS.R


public class EditorFragment : Fragment() {
    lateinit var location: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        location = view.findViewById(R.id.locationPicker)
        location.setOnClickListener {
            Log.d(this::class.java.name,"Location clicked")
        }
    }
}