package com.heinecke.aron.seesm.data


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParser
import com.heinecke.aron.seesm.*
import com.heinecke.aron.seesm.ui.login.LoggedInUserView
import okhttp3.*
import java.io.IOException

private const val API_KEY_USERNAME = "name"

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val client = OkHttpClient();

    fun login(apiToken: String, endpoint: String, userID: Int, liveData: MutableLiveData<Result<LoggedInUserView>>) {
        val request = Utils.buildAPI(endpoint,"users/$userID",apiToken).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(this@LoginDataSource::class.java.name,"onFailure",e)
                liveData.postValue(Result.Error(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.use {
                    val code = response.code
                    val res: Result<LoggedInUserView> = if (code == 200) {
                        // TODO: evaluate streaming
                        val bodyString = it!!.string()
                        val answer = JsonParser.parseString(bodyString)
                        if (answer.isJsonObject) {
                            val jsonObj = answer.asJsonObject
                            if (jsonObj.has(API_KEY_USERNAME)) {
                                Result.Success(LoggedInUserView(jsonObj.get(API_KEY_USERNAME).asString))
                            } else {
                                Log.w(this@LoginDataSource::class.java.name, "Response: $bodyString")
                                Result.Error(InvalidUserIDException())
                            }
                        } else {
                            Result.Error(InvalidResponseException(code, it.toString()))
                        }
                    } else if (code == 401) {
                        Result.Error(UnauthorizedException())
                    } else {
                        Result.Error(InvalidResponseException(code, it.toString()))
                    }

                    liveData.postValue(res)
                }
            }
        })
    }

    fun logout() {
        // TODO: revoke authentication?!
    }
}

