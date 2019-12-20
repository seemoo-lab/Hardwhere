package com.heinecke.aron.LARS.ui.editor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Selectable

class SelectorViewModel : ViewModel() {
    internal val data: MutableLiveData<MutableList<Selectable>> = MutableLiveData(mutableListOf())
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    /**
     * Storage for last type to allow cross-select persistence
     */
    internal val lastType: MutableLiveData<Selectable.SelectableType?> = MutableLiveData(null)
    /**
     * Selected item, call resetSelected once a value got selected
     */
    val selected: MutableLiveData<SelectorFragment.SelectorData?> =
        MutableLiveData()

    fun setSelected(item: SelectorFragment.SelectorData) {
        selected.value = item
    }

    /**
     * Reset item in selected
     */
    fun resetSelected() {
        selected.value = null
        searchString.value = ""
    }

    /**
     * Reset search string, internal use
     */
    fun resetSearchString() {
        Log.d(this::class.java.name,"ResetSearchString")
        searchString.value = ""
    }
}