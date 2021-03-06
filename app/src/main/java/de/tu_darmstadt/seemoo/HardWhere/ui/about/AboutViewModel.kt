package de.tu_darmstadt.seemoo.HardWhere.ui.about

import android.content.Context
import android.icu.text.DateFormat.getDateTimeInstance
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tu_darmstadt.seemoo.HardWhere.BuildConfig
import de.tu_darmstadt.seemoo.HardWhere.R
import kotlinx.coroutines.launch
import org.acra.ACRA
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        val date: String = getDateTimeInstance().format(BuildConfig.BUILD_TIME)

        value = "Large Accessories Retrieval System, copyright SEEMOO\nBuild: $date \nCommit: ${BuildConfig.GitHash}\nIncluded libraries & licenses:"
    }
    val text: LiveData<String> = _text
    private val _libList: MutableLiveData<List<AboutFragment.About>> = MutableLiveData(listOf())
    val libList: LiveData<List<AboutFragment.About>> = _libList

    val _currentLicenseText: MutableLiveData<String?> = MutableLiveData()
    val currentLicenseText: LiveData<String?> = _currentLicenseText
    fun resetLicenseText() {
        _currentLicenseText.postValue(null)
    }
    fun loadData(context: Context) {
        if(!_libList.value.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            try {
                val buffer = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.licenses)))
                val list: List<AboutFragment.About> = buffer.lines().map { line ->
                    val split = line.split(',')
                    AboutFragment.About(split[0], resolveLicense(split[1]))
                }.collect(Collectors.toList())
                val sorted = list.sortedBy {
                    it.name
                }
                Log.d(this@AboutViewModel::class.java.name,"Loaded $list")
                _libList.postValue(sorted)
            } catch (e: Exception) {
                Log.wtf("Unable to load about resources",e)
                ACRA.errorReporter.handleException(e)
            }
        }
    }

    fun loadLicense(context: Context, resource: Int) {
        viewModelScope.launch {
            try {
                val buffer = BufferedReader(InputStreamReader(context.resources.openRawResource(resource)))
                _currentLicenseText.postValue(buffer.readText())
            } catch (e: Exception) {
                Log.wtf("Unable to load license",e)
                ACRA.errorReporter.handleException(e)
            }
        }
    }

    private fun resolveLicense(input: String): Int {
        return when(input.trim()) {
            "apache2" -> R.raw.apache2
            "flaticon" -> R.raw.flaticon
            else -> throw Exception("Unknown license resource $input")
        }
    }
}