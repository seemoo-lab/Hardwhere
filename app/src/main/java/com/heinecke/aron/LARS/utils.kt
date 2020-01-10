package com.heinecke.aron.LARS

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.heinecke.aron.LARS.data.model.Asset
import okhttp3.Request
import retrofit2.Response


const val API_KEY_STATUS = "status"
const val API_KEY_MSG = "message"

class Utils {
    companion object {
        /**
         * Finds all equal asset attributes in [assets] and set these on [displayAsset]
         */
        fun getEqualAssetAttributes(displayAsset: Asset, assets: ArrayList<Asset>) {
            if(assets.size == 0)
                return
            val firstAsset = assets[0]
            var name = firstAsset.name
            var category = firstAsset.category
            var location = firstAsset.rtd_location
            var model = firstAsset.model
            var comment = firstAsset.notes

            for ((index, value) in assets.withIndex()) {
                if( index == 0)
                    continue

                if(name != null && name != value.name) name = null
                if(comment != null && comment != value.name) comment = null
                if(category != null && category != value.category) category = null
                if(location != null && location != value.rtd_location) location = null
                if(model != null && model != value.model) model = null
            }

            if(!name.isNullOrEmpty()) displayAsset.name = name
            if(!comment.isNullOrEmpty()) displayAsset.notes = comment
            if(category != null) displayAsset.category = category
            if(location != null) displayAsset.rtd_location = location
            if(model != null) displayAsset.model = model
        }

        fun stripEndpint(endpoint: String): String {
            return endpoint.trim().trimEnd('/')
        }

        fun makeApiURL(endpoint: String, path: String): String {
            return "$endpoint/api/v1/$path"
        }

        fun buildAPI(endpoint: String, path: String, apiToken: String): Request.Builder {
            val url = makeApiURL(stripEndpint(endpoint), path)
            Log.d(this::class.java.name, "URL: $url")
            return Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiToken")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
        }

        fun <T, C> logResponseVerbose(cl: Class<C>, resp: Response<T>?) {
            Log.d(
                cl::class.java.name,
                "Response: isSuccessfull: ${resp?.isSuccessful} Code: ${resp?.code()} Body: ${resp?.body()}"
            )
        }

        fun vibrate(context: Context, ms: Long) {

            if (Build.VERSION.SDK_INT >= 26) {
                val effect = VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE)
                (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(effect)
            } else {
                (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(ms)
            }
        }

        @JvmField
        val KEY_ENDPOINT = "endpoint_api"
        @JvmField
        val KEY_TOKEN = "token_api"
        @JvmField
        val PREFS_APP = "com.heinecke.aron.LARS.prefs"
        @JvmField
        val PREFS_KEY_FIRST_RUN = "first_run"
        @JvmField
        val PREFS_KEY_UID = "uid"
        @JvmField
        val PREFS_KEY_BACKEND = "backend"
        @JvmField
        val PREFS_KEY_TOKEN = "token"
    }
}