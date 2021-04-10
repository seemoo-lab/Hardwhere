package de.tu_darmstadt.seemoo.LARS.ui.lib

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ProgressViewModel: ViewModel() {
    private val _loading: MutableLiveData<Int> = MutableLiveData(0)

    /**
     * Loading indicator when > 0
     */
    val loading: LiveData<Int> = _loading

    /**
     * Error consisting out of text [@StringRes] ID and optional details
     */
    internal val _error: MutableLiveData<Pair<Int,Throwable?>?> = MutableLiveData()

    /**
     * Error container, should be reset via [resetError]
     */
    val error: LiveData<Pair<Int,Throwable?>?> = _error

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

    /**
     * Reset error value, call this after receiving a new error value
     */
    fun resetError() {
        _error.value = null
    }
}