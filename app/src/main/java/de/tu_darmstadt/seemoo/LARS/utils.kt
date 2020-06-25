package de.tu_darmstadt.seemoo.LARS

import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
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

        fun displayToastUp(context: Context, @StringRes text: Int, duration: Int) {
            val toast = Toast.makeText(context, text, duration)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0,0)
            toast.show()
        }

        fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
            return if (p1 != null && p2 != null) block(p1, p2) else null
        }
        fun <T1: Any, T2: Any, T3: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3)->R?): R? {
            return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
        }
        fun <T1: Any, T2: Any, T3: Any, T4: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4)->R?): R? {
            return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
        }
        fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5)->R?): R? {
            return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
        }

        fun playErrorBeep() {
            val toneG = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneG.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
//            toneG.startTone(ToneGenerator.TONE_CDMA_LOW_PBX_L, 200)
        }

        private fun stripEndpint(endpoint: String): String {
            return endpoint.trim().trimEnd('/')
        }

        private fun makeApiURL(endpoint: String, path: String): String {
            return "$endpoint/api/v1/$path"
        }

        fun buildAPI(endpoint: String, path: String, apiToken: String): Request.Builder {
            val url = makeApiURL(
                stripEndpint(
                    endpoint
                ),
                path
            )
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

        fun hideKeyboard(activity: Activity) {
            val inputManager: InputMethodManager = activity
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // check if no view has focus:
            val currentFocusedView: View? = activity.currentFocus
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(
                    currentFocusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        fun hideKeyboardContext(
            context: Context,
            view: View
        ) {
            val imm =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        @JvmField
        val DEFAULT_LOAD_AMOUNT = 50

        @JvmField
        val KEY_ENDPOINT = "endpoint_api"
        @JvmField
        val KEY_TOKEN = "token_api"
        @JvmField
        val PREFS_APP = "de.tu_darmstadt.seemoo.LARS.prefs"
        @JvmField
        val PREFS_KEY_FIRST_RUN = "first_run"
        @JvmField
        val PREFS_KEY_UID = "uid"
        @JvmField
        val PREFS_KEY_BACKEND = "backend"
        @JvmField
        val PREFS_KEY_TOKEN = "token"
        @JvmField
        val ITEM_OLDAGE_MS = 1000*60*5
    }
}