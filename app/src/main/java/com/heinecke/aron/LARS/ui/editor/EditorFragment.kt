package com.heinecke.aron.LARS.ui.editor

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable
import com.heinecke.aron.LARS.ui.APIFragment


class EditorFragment : APIFragment() {
    lateinit var editorViewModel: EditorViewModel
    lateinit var selectorViewModel: SelectorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorViewModel = ViewModelProviders.of(requireActivity())[EditorViewModel::class.java]
        if (savedInstanceState == null) {
            arguments!!.run {
                editorViewModel.setEditorAsset(this.getParcelable(PARAM_ASSET)!!)
                editorViewModel.multiEditAssets.value = this.getParcelableArrayList(PARAM_MULTIEDIT)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.editor, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.finishEdit -> {
                Log.d(this::class.java.name, "finishing editor")
                editorViewModel.updateAssets(getAPI())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location: EditText = view.findViewById(R.id.locationPicker)
        val model: EditText = view.findViewById(R.id.modelPicker)
        val category: EditText = view.findViewById(R.id.categoryPicker)
        val comment: EditText = view.findViewById(R.id.commentEditor)

        setupSelectable(location,Selectable.SelectableType.Location, R.id.locationPicker) {editorViewModel.asset.value!!.rtd_location}
        setupSelectable(model,Selectable.SelectableType.Model, R.id.modelPicker) {editorViewModel.asset.value!!.model}
        setupSelectable(category,Selectable.SelectableType.Category, R.id.categoryPicker) {editorViewModel.asset.value!!.category}

        selectorViewModel = ViewModelProviders.of(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            Log.d(this@EditorFragment::class.java.name,"Selected: $it")
            it?.run {
                val currentVal = editorViewModel.asset.value!!
                when (it.inputID) {
                    R.id.locationPicker -> currentVal.rtd_location = it.item as Selectable.Location
                    R.id.modelPicker -> currentVal.model = it.item as Selectable.Model
                    R.id.categoryPicker -> currentVal.category= it.item as Selectable.Category
                    else -> Log.w(
                        this@EditorFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer { it ->
            Log.d(this::class.java.name,"Asset update $it")
            it?.run {
                location.setText(this.rtd_location?.name ?: "")
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                comment.setText(this.notes)
            }
        })
    }

    private fun <T: Selectable> setupSelectable(et: EditText, type: Selectable.SelectableType,
                                                returnCode: Int, v: () -> T?) {
        et.setOnClickListener {
            Log.d(this::class.java.name, "$type clicked")
            val (id, args) = SelectorFragment.newInstancePair(
                v(),
                returnCode,
                type
            )
            findNavController().navigate(id, args)
        }
    }

    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstancePair(asset: Asset, multiedit: ArrayList<Asset>): Pair<Int, Bundle> {
            val args = Bundle()
            args.putParcelable(PARAM_ASSET, asset)
            args.putParcelableArrayList(PARAM_MULTIEDIT, multiedit)
            return Pair(R.id.editorFragment, args)
        }

        private const val PARAM_ASSET = "asset"
        private const val PARAM_MULTIEDIT = "multiedit"
    }
}