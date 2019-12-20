package com.heinecke.aron.LARS.ui.editor

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
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable
import com.heinecke.aron.LARS.ui.APIFragment


class EditorFragment : APIFragment() {
    lateinit var editorViewModel: EditorViewModel
    lateinit var selectorViewModel: SelectorViewModel
    private lateinit var comment: EditText
    private lateinit var tag: EditText
    private lateinit var name: EditText

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
                val asset = editorViewModel.asset.value!!
                asset.notes = comment.text.toString()
                asset.asset_tag = tag.text.toString()
                asset.name = name.text.toString()
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
        comment = view.findViewById(R.id.commentEditor)
        tag = view.findViewById(R.id.assetTag)
        name = view.findViewById(R.id.assetName)
        val loading: ProgressBar = view.findViewById(R.id.loading)

        setupSelectable(location,Selectable.SelectableType.Location, R.id.locationPicker) {editorViewModel.asset.value!!.rtd_location}
        setupSelectable(model,Selectable.SelectableType.Model, R.id.modelPicker) {editorViewModel.asset.value!!.model}
        setupSelectable(category,Selectable.SelectableType.Category, R.id.categoryPicker) {editorViewModel.asset.value!!.category}

        val multiEdit = editorViewModel.multiEditAssets.value!!.size > 1
        if (multiEdit) {
            tag.setOnClickListener {
                Toast.makeText(requireContext(),R.string.toast_no_tag_multiedit,Toast.LENGTH_SHORT).show()
            }
        }

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
                selectorViewModel.resetSelected()
            }
        })

        editorViewModel.asset.observe(viewLifecycleOwner, Observer { it ->
            Log.d(this::class.java.name,"Asset update $it")
            it?.run {
                location.setText(this.rtd_location?.name ?: "")
                model.setText(this.model?.name ?: "")
                category.setText(this.category?.name ?: "")
                comment.setText(this.notes)
                tag.setText(this.asset_tag)
                this@EditorFragment.name.setText(this.name)
            }
        })

        editorViewModel.loading.observe(this, Observer {
            Log.d(this@EditorFragment::class.java.name,"Loading-Update: $it")
            loading.visibility = if(it == null) View.INVISIBLE else View.INVISIBLE
            if (it != null) {
                if (it.success != null) {
                    if (it.success == true) {
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(),"Failed: ${it.error}",Toast.LENGTH_LONG).show()
                    }
                    editorViewModel.loading.value = null // reset
                }
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