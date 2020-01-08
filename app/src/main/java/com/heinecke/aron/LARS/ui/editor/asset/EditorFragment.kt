package com.heinecke.aron.LARS.ui.editor.asset

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable
import com.heinecke.aron.LARS.ui.APIFragment
import com.heinecke.aron.LARS.ui.editor.SelectorFragment
import com.heinecke.aron.LARS.ui.editor.SelectorViewModel

/**
 * Asset editor fragment
 */
class EditorFragment : APIFragment() {
    lateinit var editorViewModel: EditorViewModel
    lateinit var selectorViewModel: SelectorViewModel
    private lateinit var commentET: EditText
    private lateinit var tagET: EditText
    private lateinit var nameET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorViewModel = ViewModelProviders.of(requireActivity())[EditorViewModel::class.java]
        if (savedInstanceState == null) {
            arguments!!.run {
                editorViewModel.multiEditAssets.value = this.getParcelableArrayList(
                    PARAM_ASSETS
                )
                with(editorViewModel.multiEditAssets.value!!) {
                    if(size == 1) {
                        editorViewModel.setEditorAsset(this[0])
                    } else {
                        val displayAsset = Asset.getEmptyAsset(true)
                        // display equal attributes for all assets
                        Utils.getEqualAssetAttributes(displayAsset,this)
                        editorViewModel.setEditorAsset(displayAsset)
                    }
                }
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
                with(editorViewModel.asset.value!!) {
                    notes = commentET.text.toString()
                    asset_tag = tagET.text.toString()
                    name = nameET.text.toString()
                }
                editorViewModel.updateAssets(getAPI())
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
        commentET = view.findViewById(R.id.commentEditor)
        tagET = view.findViewById(R.id.assetTag)
        nameET = view.findViewById(R.id.assetName)
        val loading: ProgressBar = view.findViewById(R.id.loading)

        setupSelectable(
            location,
            Selectable.SelectableType.Location,
            R.id.locationPicker
        ) { editorViewModel.asset.value!!.rtd_location }
        setupSelectable(
            model,
            Selectable.SelectableType.Model,
            R.id.modelPicker
        ) { editorViewModel.asset.value!!.model }
        setupSelectable(
            category,
            Selectable.SelectableType.Category,
            R.id.categoryPicker
        ) { editorViewModel.asset.value!!.category }

        val multiEdit = editorViewModel.multiEditAssets.value!!.size > 1
        tagET.isFocusable = !multiEdit
        if (multiEdit) {
            tagET.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_no_tag_multiedit,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        selectorViewModel = ViewModelProviders.of(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
            Log.d(this@EditorFragment::class.java.name, "Selected: $it")
            it?.run {
                val currentVal = editorViewModel.asset.value!!
                when (it.inputID) {
                    R.id.locationPicker -> currentVal.rtd_location = it.item as Selectable.Location
                    R.id.modelPicker -> currentVal.model = it.item as Selectable.Model
                    R.id.categoryPicker -> currentVal.category = it.item as Selectable.Category
                    else -> Log.w(
                        this@EditorFragment::class.java.name,
                        "Unknown inputID for selector update"
                    )
                }
                selectorViewModel.resetSelected()
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer { it ->
            Log.d(this::class.java.name, "Asset update $it")
            it?.run {
                location.setText(this.rtd_location?.name ?: "")
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                commentET.setText(this.notes)
                tagET.setText(this.asset_tag)
                this@EditorFragment.nameET.setText(this.name)
            }
        })

        editorViewModel.loading.observe(this, Observer {
            Log.d(this@EditorFragment::class.java.name, "Loading-Update: $it")
            loading.visibility = if (it == null) View.INVISIBLE else View.INVISIBLE
            if (it != null) {
                if (it.success != null) {
                    if (it.success == true) {
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${it.error}", Toast.LENGTH_LONG)
                            .show()
                    }
                    editorViewModel.loading.value = null // reset
                }
            }
        })
    }

    private fun <T : Selectable> setupSelectable(
        et: EditText, type: Selectable.SelectableType,
        returnCode: Int, v: () -> T?
    ) {
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
         * Create a new navigation resource ID and param bundle pair to be used with nagController.
         * Multi-edit features are determined on the amount of elements in [assets]
         */
        @JvmStatic
        fun newInstancePair(assets: ArrayList<Asset>): Pair<Int, Bundle> {
            val args = Bundle()
            args.putParcelableArrayList(PARAM_ASSETS, assets)
            return Pair(R.id.editorFragment, args)
        }

        private const val PARAM_ASSETS = "assets"
    }
}