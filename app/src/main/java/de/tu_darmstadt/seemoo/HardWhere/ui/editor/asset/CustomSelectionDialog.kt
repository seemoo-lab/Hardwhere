package de.tu_darmstadt.seemoo.HardWhere.ui.editor.asset

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Dialog for selecting custom values (checkbox fields)
 */
class CustomSelectionDialog: DialogFragment() {
    lateinit var customSelectionViewModel: CustomSelectionViewModel
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        customSelectionViewModel = ViewModelProvider(requireActivity())[CustomSelectionViewModel::class.java]
        val items = customSelectionViewModel.items
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(customSelectionViewModel.title)
            .setItems(items) { dialog, which ->
                customSelectionViewModel._selection.value = items[which]
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        fun newInstance(items: Array<String>, title: String, activity: ViewModelStoreOwner): CustomSelectionDialog {
            val customSelectionViewModel = ViewModelProvider(activity)[CustomSelectionViewModel::class.java]
            customSelectionViewModel.items = items
            customSelectionViewModel.title = title
            return CustomSelectionDialog()
        }
    }
}