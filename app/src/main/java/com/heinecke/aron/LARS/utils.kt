package com.heinecke.aron.LARS

import android.util.Log
import okhttp3.Request
import retrofit2.Response

const val API_KEY_STATUS = "status"
const val API_KEY_MSG = "message"

class Utils {
    companion object {
        fun stripEndpint(endpoint: String): String {
            return endpoint.trim().trimEnd('/')
        }

        fun makeApiURL(endpoint: String, path: String): String {
            return "$endpoint/api/v1/$path"
        }

        fun buildAPI(endpoint: String, path: String, apiToken: String): Request.Builder {
            val url = makeApiURL(stripEndpint(endpoint), path)
            Log.d(this::class.java.name,"URL: $url")
            return Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer $apiToken")
                .addHeader("Accept","application/json")
                .addHeader("Content-Type","application/json")
        }

        fun <T,C> logResponseVerbose(cl: Class<C>, resp: Response<T>?) {
            Log.d(cl::class.java.name, "Response: isSuccessfull: ${resp?.isSuccessful} Code: ${resp?.code()} Body: ${resp?.body()}")
        }

        @JvmField val KEY_ENDPOINT = "endpoint_api"
        @JvmField val KEY_TOKEN = "token_api"
        @JvmField val PREFS_APP = "com.heinecke.aron.LARS.prefs"
        @JvmField val PREFS_KEY_FIRST_RUN = "first_run"
        @JvmField val PREFS_KEY_UID = "uid"
        @JvmField val PREFS_KEY_BACKEND = "backend"
        @JvmField val PREFS_KEY_TOKEN = "token"
    }
}