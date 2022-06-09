package de.tu_darmstadt.seemoo.HardWhere.ui.editor.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable

class CustomSelectionViewModel : ViewModel() {
    var title: String = "<missing title>"
    var items: Array<String> = arrayOf()
    var identifier: String = "<missing identifier>"

    internal val _selection: MutableLiveData<String?> = MutableLiveData()
    val selection: LiveData<String?> = _selection

    fun resetSelection() {
        _selection.value = null
    }
}