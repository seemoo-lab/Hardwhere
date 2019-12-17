package com.heinecke.aron.LARS.ui.editor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable


public class EditorFragment : Fragment() {
    lateinit var location: EditText
    lateinit var editorViewModel: EditorViewModel
    lateinit var selectorViewModel: SelectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorViewModel = ViewModelProviders.of(requireActivity())[EditorViewModel::class.java]
        if (savedInstanceState == null) {
            arguments!!.run {
                editorViewModel.setEditorAsset(this.getParcelable(PARAM_ASSET)!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        location = view.findViewById(R.id.locationPicker)
        location.setOnClickListener {
            Log.d(this::class.java.name, "Location clicked")
            val (id, args) = SelectorFragment.newInstancePair(
                editorViewModel.asset.value!!.rtd_location,
                R.id.locationName,
                Selectable.SelectableType.Location
            )
            findNavController().navigate(id, args)
        }

        selectorViewModel = ViewModelProviders.of(requireActivity())[SelectorViewModel::class.java]

        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            Log.d(this@EditorFragment::class.java.name,"Selected: $it")
            it?.run {
                val currentVal = editorViewModel.asset.value!!
                when (it.inputID) {
                    R.id.locationName -> currentVal.rtd_location = it.item as Selectable.Location
                    else -> Log.w(
                        this@EditorFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setText(this.rtd_location?.name ?: "")
            }
        })
    }

    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstancePair(asset: Asset): Pair<Int, Bundle> {
            val args = Bundle()
            args.putParcelable(PARAM_ASSET, asset)
            return Pair(R.id.editorFragment, args)
        }

        private const val PARAM_ASSET = "asset"
    }
}