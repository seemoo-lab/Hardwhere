package com.heinecke.aron.seesm.data

import com.heinecke.aron.seesm.InvalidResponseException
import com.heinecke.aron.seesm.UnauthorizedException
import com.heinecke.aron.seesm.Utils
import okhttp3.*
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val client = OkHttpClient();

    fun login(apiToken: String, endpoint: String): Result<Unit> {
        try {
            val request = Utils.buildAPI(endpoint,"users",apiToken).build()
            client.newCall(request).execute().use {
                return if (it.code() == 200) {
                    Result.Success(Unit)
                } else if(it.code() == 401) {
                    Result.Error(UnauthorizedException())
                } else {
                    Result.Error(InvalidResponseException(it.code(), it.body().toString()))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication?!
    }
}

