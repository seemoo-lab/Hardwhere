package de.tu_darmstadt.seemoo.HardWhere

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.HardWhere.data.model.LoginData
import de.tu_darmstadt.seemoo.HardWhere.data.model.UserData

class MainViewModel : ViewModel() {
    /**
     * Scan data which is non-null if new asset codes were scanned
     */
    val scanData = MutableLiveData<Int?>()
    val loginData = MutableLiveData<LoginData?>(null)
    val userData = MutableLiveData<UserData?>(null)
    fun getLoginData(context: Context): LoginData? {
        var data = loginData.value
        if (data == null) {
            val prefs = context.getSharedPreferences(Utils.PREFS_APP, 0)
            val uid = prefs.getInt(Utils.PREFS_KEY_UID, 0)
            val token = prefs.getString(Utils.PREFS_KEY_TOKEN, null)
            val backend = prefs.getString(Utils.PREFS_KEY_BACKEND, null)
            if (token != null && backend != null) {
                data = LoginData(
                    uid,
                    token,
                    backend
                )
                loginData.postValue(data)
            }
        }
        return data
    }

    fun requireLoginData(context: Context) : LoginData = getLoginData(context)!!
}