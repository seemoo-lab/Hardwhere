package de.tu_darmstadt.seemoo.LARS.ui.editor.asset

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.CustomField
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
    private lateinit var containerCustomAttribs: LinearLayout
    private lateinit var infoMultipleModelTV: TextView

    /**
     * Custom fields element storage to prevent re-creation on every change call
     */
    private var customFields: HashMap<String,AssetAttributeView> = HashMap()

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
        val model: AssetAttributeView = view.findViewById(R.id.modelPicker)
        val category: EditText = view.findViewById(R.id.categoryPicker)
        containerCustomAttribs = view.findViewById(R.id.frag_editor_attrib_container)
        commentET = view.findViewById(R.id.commentEditor)
        nameET = view.findViewById(R.id.assetName)
        tagET = view.findViewById(R.id.assetTag)
        infoMultipleModelTV = view.findViewById(R.id.info_multiple_models)


        val loading: ProgressBar = view.findViewById(R.id.loading)

        // always store state change
        // workaround for android bug: if navigated away, no onSaveInstanceState is called
        // this happens due to a misbehavior in the navigation component
        setupTextfield(commentET,{t -> editorViewModel.asset.value?.notes = t}) {
            a,o -> a.notes = o.notes
        }
        setupTextfield(tagET,{t -> editorViewModel.asset.value?.asset_tag = t}) {
                a,o -> a.asset_tag = o.asset_tag
        }
        setupTextfield(nameET,{t -> editorViewModel.asset.value?.name = t}) {
                a,o -> a.name = o.name
        }

        setupSelectable(
            location,
            Selectable.SelectableType.Location,
            R.id.locationPicker,
            { editorViewModel.asset.value!!.rtd_location }
        )
        {
            val asset = editorViewModel.asset.value!!
            asset.rtd_location = editorViewModel.assetOrigin.value!!.rtd_location
            editorViewModel.assetMutable.value = asset
        }
        setupSelectable(
            model,
            Selectable.SelectableType.Model,
            R.id.modelPicker,
            { editorViewModel.asset.value!!.model }
        ) { val asset = editorViewModel.asset.value!!
            asset.model = editorViewModel.assetOrigin.value!!.model
            editorViewModel.assetMutable.value = asset }

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
                editorViewModel.assetMutable.value = currentVal
                selectorViewModel.resetSelected()
            }
        })

        // first handle origin, then current
        editorViewModel.assetOrigin.observe(viewLifecycleOwner, Observer { it ->
            it?.run {
                location.setDefaultText(this.rtd_location?.name)
                model.setDefaultText(this.model?.name ?: "")
//                category.setText(this.category?.name ?: "")
                commentET.setDefaultText(this.notes)
                tagET.setDefaultText(this.asset_tag)
                this@EditorFragment.nameET.setDefaultText(this.name)
                this.custom_fields?.run { updateCustomFields(this, update = true,updateDefault = true) }
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer {
            it?.run {
                location.setText(this.rtd_location?.name)
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                commentET.setText(this.notes)
                tagET.setText(this.asset_tag)
                this@EditorFragment.nameET.setText(this.name)
                this.custom_fields?.run { updateCustomFields(this, update = true, updateDefault = false) }
            }
        })

        editorViewModel.customtomAttributeFields.observe(viewLifecycleOwner, {
            it?.run {
                if(editorViewModel.asset.value!!.custom_fields != null)
                    updateCustomFields(editorViewModel.asset.value!!.custom_fields!!, false, false)

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

    /**
     * Update custom fields with specified fields. Sets current value of default depending on [updateDefault]
     */
    private fun updateCustomFields(fields: HashMap<String, CustomField>, update: Boolean, updateDefault: Boolean) {
        Log.d(this::class.java.name, "updateCustomFields called $update $updateDefault")
        val inflater = layoutInflater
        editorViewModel.customtomAttributeFields.value?.run {
            fields.keys.removeIf{k -> !this.contains(k)}
        }

        customFields = customFields.filterTo(HashMap()) {
            (key, view) -> fields[key]?.run {
                if(updateDefault)
                    view.setDefaultText(this.value)
                else if(update)
                    view.setText(this.value)
                true
            } ?: run {
                containerCustomAttribs.removeView(view)
                false
            }
        }
        for (attrib in fields) {
            if (!customFields.containsKey(attrib.key)) {
                val view = AssetAttributeView(requireContext())
                containerCustomAttribs.addView(view)
                view.tag = attrib.value.field
                view.setText(attrib.value.value)
                view.setLabel(attrib.key)
                view.setDefaultText(attrib.value.value)
                setupTextfield(view,{t -> run{
                    if (editorViewModel.asset.value?.custom_fields == null) {
                        editorViewModel.asset.value?.custom_fields = HashMap()
                    }
                    editorViewModel.asset.value?.custom_fields?.put(attrib.key,attrib.value.copy(value = t))
                }
                }) {
                        a,o ->
                    o.custom_fields!!.get(attrib.key)!!.let { a.custom_fields!!.put(attrib.key, it) }
                }
                customFields.put(attrib.key,view)
            }
        }
        val singleModel = editorViewModel.isSingleModel()
        Log.d(this::class.java.name, "Single Model: $singleModel")
        infoMultipleModelTV.visibility = if(singleModel) View.GONE else View.VISIBLE
    }

    /**
     * Setup text field with reset functions.
     * [setVal] receives a value on text change to update the internal value.
     * [reset] is called when update is unchecked, to reset the value
     */
    private fun setupTextfield(
        et: AssetAttributeView, setVal: (t: String) -> Unit, reset: (a: Asset, orig: Asset) -> Unit
    ) {
        et.setTextChangedListener { text -> setVal.invoke(text) }
        et.setOnCheckedChangeListener { c ->
            if (!c) {
                val asset = editorViewModel.asset.value!!
                reset.invoke(asset, editorViewModel.assetOrigin.value!!)
                editorViewModel.assetMutable.value = asset
            }
        }
    }

    /**
     * reset lambda is to reset on change-uncheck
     */
    private fun <T : Selectable> setupSelectable(
        et: AssetAttributeView, type: Selectable.SelectableType,
        returnCode: Int, v: () -> T? , reset: () -> Unit
    ) {
        et.setEditorOnclickListener(View.OnClickListener {
            val (id, args) = SelectorFragment.newInstancePair(
                v(),
                returnCode,
                type
            )
            findNavController().navigate(id, args)
        })
        et.setOnCheckedChangeListener { checked -> if (!checked) {
            reset()
            Log.d(this::class.java.name,"Resetting")
        }
        }
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