package de.tu_darmstadt.seemoo.LARS.ui.lib

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ProgressViewModel: ViewModel() {
    private val _loading: MutableLiveData<Int> = MutableLiveData(0)
    val loading: LiveData<Int> = _loading

    internal fun decLoading() {
        val value = _loading.value!!
        val newValue = if (value > 0)
            value - 1
        else
            0
        _loading.postValue(newValue)
    }

    internal fun incLoading() {
        _loading.postValue(_loading.value!! + 1)
    }
}