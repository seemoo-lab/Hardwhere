package com.heinecke.aron.LARS.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.BuildConfig

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Build: ${BuildConfig.BUILD_TIME} \nCommit: ${BuildConfig.GitHash}"
    }
    val text: LiveData<String> = _text
}