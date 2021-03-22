package de.tu_darmstadt.seemoo.LARS.ui.lenting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.BuildConfig

class LentingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Large Accessories Retrieval System\nBuild: ${BuildConfig.BUILD_TIME} \nCommit: ${BuildConfig.GitHash}"
    }
    val text: LiveData<String> = _text
}