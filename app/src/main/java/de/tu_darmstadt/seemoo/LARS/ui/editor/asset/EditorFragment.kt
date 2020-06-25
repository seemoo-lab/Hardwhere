package de.tu_darmstadt.seemoo.LARS.ui.editor.asset

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.APIFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.AssetAttributeView
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorFragment
import de.tu_darmstadt.seemoo.LARS.ui.editor.SelectorViewModel

/**
 * Asset editor fragment
 */
class EditorFragment : APIFragment() {
    lateinit var editorViewModel: EditorViewModel
    lateinit var selectorViewModel: SelectorViewModel
    private lateinit var commentET: AssetAttributeView
    private lateinit var tagET: AssetAttributeView
    private lateinit var nameET: AssetAttributeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorViewModel = ViewModelProvider(requireActivity())[EditorViewModel::class.java]
        if (savedInstanceState == null) {
            requireArguments().run {
                editorViewModel.multiEditAssets.value = this.getParcelableArrayList(
                    PARAM_ASSETS
                )
                with(editorViewModel.multiEditAssets.value!!) {
                    Log.d(this::class.java.name,"First run, setting values..")
                    if (size == 1) {
                        editorViewModel.setEditorAsset(this[0])
                    } else {
                        val displayAsset = Asset.getEmptyAsset(true)
                        // display equal attributes for all assets
                        Utils.getEqualAssetAttributes(displayAsset, this)
                        editorViewModel.setEditorAsset(displayAsset)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("asd",1)
        // store text fields back
        editorViewModel.asset.value!!.run {
            this.name = nameET.getText().toString()
            this.notes = commentET.getText().toString()
            Log.d(this::class.java.name,"Storing: $this")
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
                    notes = commentET.getValue()
                    asset_tag = tagET.getValue()
                    name = nameET.getValue()
                }
                editorViewModel.updateAssets(getAPI())
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location: AssetAttributeView = view.findViewById(R.id.locationPicker)
        val model: EditText = view.findViewById(R.id.modelPicker)
        val category: EditText = view.findViewById(R.id.categoryPicker)
        commentET = view.findViewById(R.id.commentEditor)
        nameET = view.findViewById(R.id.assetName)
        tagET = view.findViewById(R.id.assetTag)


        val loading: ProgressBar = view.findViewById(R.id.loading)

        // workaround android bug: if navigated away, no onSaveInstanceState is called
        // this happens due to a misbehavior in the navigation component
        commentET.setTextChangedListener {text -> editorViewModel.asset.value?.notes = text }
        tagET.setTextChangedListener {text -> editorViewModel.asset.value?.asset_tag = text }
        nameET.setTextChangedListener {text -> editorViewModel.asset.value?.name = text }

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

        // disable category, can't be edited on asset, model attribute
        category.isFocusableInTouchMode = false
        category.isLongClickable = false
        category.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                requireContext(),
                R.string.asset_category_uneditable,
                Toast.LENGTH_LONG
            ).show()
        })

        val multiEdit = editorViewModel.multiEditAssets.value!!.size > 1
//        tagET.isFocusable = !multiEdit
        tagET.isEnabled = !multiEdit
        if (multiEdit) {
            tagET.setOnClickListener {
                Utils.displayToastUp(requireContext(),R.string.toast_no_tag_multiedit,Toast.LENGTH_SHORT)
            }
        }

        selectorViewModel = ViewModelProvider(requireActivity())[SelectorViewModel::class.java]
        selectorViewModel.selected.observe(viewLifecycleOwner, Observer {
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

        // first handle origin, then current
        editorViewModel.assetOrigin.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setDefaultText(this.rtd_location?.name)
//                model.setText(this.model?.name ?: "")
//                category.setText(this.category?.name ?: "")
                commentET.setDefaultText(this.notes)
                tagET.setDefaultText(this.asset_tag)
                this@EditorFragment.nameET.setDefaultText(this.name)
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setText(this.rtd_location?.name)
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                commentET.setText(this.notes)
                tagET.setText(this.asset_tag)
                this@EditorFragment.nameET.setText(this.name)
            }
        })

        editorViewModel.loading.observe(viewLifecycleOwner, Observer {
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
        et: AssetAttributeView, type: Selectable.SelectableType,
        returnCode: Int, v: () -> T?
    ) {
        et.setEditorOnclickListener(View.OnClickListener {
            val (id, args) = SelectorFragment.newInstancePair(
                v(),
                returnCode,
                type
            )
            findNavController().navigate(id, args)
        })
    }

    private fun <T : Selectable> setupSelectable(
        et: EditText, type: Selectable.SelectableType,
        returnCode: Int, v: () -> T?
    ) {
        et.isFocusableInTouchMode = false
        et.isLongClickable = false
        et.setOnClickListener {
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