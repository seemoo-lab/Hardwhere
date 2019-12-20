package com.heinecke.aron.LARS.ui.editor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Selectable

class SelectorViewModel : ViewModel() {
    internal val data: MutableLiveData<MutableList<Selectable>> = MutableLiveData(mutableListOf())
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    /**
     * Selected item, call resetSelected once a value got selected
     */
    internal val selected: MutableLiveData<SelectorFragment.SelectorData?> =
        MutableLiveData()

    fun setSelected(item: SelectorFragment.SelectorData) {
        selected.value = item
    }
    fun resetSelected() {
        selected.value = null
        searchString.value = ""
    }
}