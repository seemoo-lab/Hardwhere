package de.tu_darmstadt.seemoo.LARS.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.BuildConfig

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Large Accessories Retrieval System\nBuild: ${BuildConfig.BUILD_TIME} \nCommit: ${BuildConfig.GitHash}"
    }
    val text: LiveData<String> = _text
}