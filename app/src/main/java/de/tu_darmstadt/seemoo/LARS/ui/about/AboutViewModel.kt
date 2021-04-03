package de.tu_darmstadt.seemoo.LARS.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.BuildConfig

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Large Accessories Retrieval System\nBuild: ${BuildConfig.BUILD_TIME} \nCommit: ${BuildConfig.GitHash}\n\nCopyright SEEMOO.\nUsing the following libraries copyright by their authors:\nrxjava\nretrofit\nokhttp\ngoogle material designs\nicons from flaticon.com"
    }
    val text: LiveData<String> = _text
}