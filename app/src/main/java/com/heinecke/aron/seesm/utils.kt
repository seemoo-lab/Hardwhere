package com.heinecke.aron.seesm

import okhttp3.Request

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
            return Request.Builder()
                .url(makeApiURL(Utils.stripEndpint(endpoint), path))
                .addHeader("Authorization",apiToken)
                .addHeader("Accept","application/json")
                .addHeader("Content-Type","application/json")
        }

        @JvmField val KEY_ENDPOINT = "endpoint_api"
        @JvmField val KEY_TOKEN = "token_api"
    }
}