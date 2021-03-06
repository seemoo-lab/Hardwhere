package de.tu_darmstadt.seemoo.HardWhere.data


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParser
import de.tu_darmstadt.seemoo.HardWhere.InvalidResponseException
import de.tu_darmstadt.seemoo.HardWhere.InvalidUserIDException
import de.tu_darmstadt.seemoo.HardWhere.UnauthorizedException
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.data.APIClient.Companion.getHttpBase
import de.tu_darmstadt.seemoo.HardWhere.data.model.LoginData
import de.tu_darmstadt.seemoo.HardWhere.ui.login.LoggedInUserView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

private const val API_KEY_USERNAME = "name"
private const val API_KEY_ERROR = "error"
private const val API_KEY_USER_ID = "id"

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    fun login(backend: String, token: String, liveData: MutableLiveData<Result<LoggedInUserView>>) {
        val request = Utils.buildAPI(backend, "users/me", token).build()
        getHttpBase(token).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(this@LoginDataSource::class.java.name, "onFailure $e")
                liveData.postValue(Result.Error(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.use {
                    val code = response.code
                    Log.d(this::class.java.name, "response: $response")
                    val res: Result<LoggedInUserView> = if (code == 200) {
                        val bodyString = it!!.string()
                        val answer = JsonParser.parseString(bodyString)
                        if (answer.isJsonObject) {
                            val jsonObj = answer.asJsonObject
                            if (jsonObj.has(API_KEY_ERROR)) {
                                // {"error":"Unauthorized."}
                                Result.Error(InvalidResponseException(code, it.toString()))
                            } else if (jsonObj.has(API_KEY_USERNAME) && jsonObj.has(API_KEY_USER_ID)) {
                                val data = LoginData(jsonObj.get(API_KEY_USER_ID).asInt,token,backend)
                                Result.Success(
                                    LoggedInUserView(
                                        jsonObj.get(API_KEY_USERNAME).asString,
                                        data
                                    )
                                )
                            } else {
                                Log.w(
                                    this@LoginDataSource::class.java.name,
                                    "Response: $bodyString"
                                )
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

