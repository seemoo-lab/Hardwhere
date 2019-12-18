package com.heinecke.aron.LARS

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.LoginData
import com.heinecke.aron.LARS.data.model.UserData

class MainViewModel : ViewModel() {
    /**
     * Scan data which is non-null if new asset codes were scanned
     */
    val scanData = MutableLiveData<Int?>()
    val loginData = MutableLiveData<LoginData>()
    val userData = MutableLiveData<UserData?>(null)
    fun getLoginData(context: Context): LoginData {
        var data = loginData.value
        if (data == null) {
            val prefs = context.getSharedPreferences(Utils.PREFS_APP, 0)

            data = LoginData(
                prefs.getInt(Utils.PREFS_KEY_UID, 0),
                prefs.getString(Utils.PREFS_KEY_TOKEN, null)!!,
                prefs.getString(Utils.PREFS_KEY_BACKEND, null)!!
            )

            loginData.postValue(data)
        }
        return data
    }
}