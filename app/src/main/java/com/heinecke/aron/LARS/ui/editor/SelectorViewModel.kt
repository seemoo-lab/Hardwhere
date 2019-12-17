package com.heinecke.aron.LARS.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Selectable

class SelectorViewModel : ViewModel() {
    internal val data: MutableLiveData<MutableList<Selectable>> = MutableLiveData(mutableListOf())
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    internal val selectorLiveData: MutableLiveData<SelectorFragment.SelectorData?> =
        MutableLiveData()
    var selected: LiveData<SelectorFragment.SelectorData?> = selectorLiveData

    fun setSelected(item: SelectorFragment.SelectorData) {
        selectorLiveData.value = item
    }
}