package de.tu_darmstadt.seemoo.LARS.data

import com.google.gson.*
import de.tu_darmstadt.seemoo.LARS.BuildConfig
import de.tu_darmstadt.seemoo.LARS.data.model.Date
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


class APIClient {
    companion object {
        private const val SIMULATE_SLOW_NETWORK: Boolean = false
        private const val DISPLAY_FULL_LOG: Boolean = false

        fun getClient(baseUrl: String, token: String): Retrofit {
            val builder =
                OkHttpClient.Builder().addInterceptor {
                    val newRequest: Request = it.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    it.proceed(newRequest)
                }
            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                if(DISPLAY_FULL_LOG)
                    interceptor.level = HttpLoggingInterceptor.Level.BODY
                else
                    interceptor.level = HttpLoggingInterceptor.Level.BASIC
                builder.addInterceptor(interceptor)

                if(SIMULATE_SLOW_NETWORK) {
                    builder.addInterceptor {
                        Thread.sleep(4000)
                        it.proceed(it.request())
                    }
                }
            }
            val client: OkHttpClient = builder.build()

            val gson = GsonBuilder().serializeNulls()
                .registerTypeAdapter(Date::class.java, DateModelDeserializer()).create()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }

    /**
     * Custom deserializer to allow patch-payloads with only the non-formated data
     */
    internal class DateModelDeserializer : JsonDeserializer<Date?> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date? {
            return if (json.isJsonObject) {
                Gson().fromJson<Date>(json, Date::class.java)
            } else {
                Date(datetime = json.asString, formatted = json.asString)
            }
        }
    }
}