package com.heinecke.aron.LARS.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Asset

class ScanViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is scan Fragment"
    }
    val scanList: ArrayList<Asset> = ArrayList()
    val text: LiveData<String> = _text
}