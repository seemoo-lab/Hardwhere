package de.tu_darmstadt.seemoo.LARS.data

import android.util.Log
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
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class APIClient {
    companion object {
        private const val SIMULATE_SLOW_NETWORK: Boolean = false
        private const val DISPLAY_FULL_LOG: Boolean = true

        class MyManager : X509TrustManager {

            override fun checkServerTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                p1: String?
            ) {
                //allow all
            }

            override fun checkClientTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                p1: String?
            ) {
                //allow all
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        }

        private fun getUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
            return try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                val trustManagerFactory: TrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers: Array<TrustManager> =
                    trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + trustManagers.contentToString()
                }

                val trustManager =
                    trustManagers[0] as X509TrustManager


                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustManager)
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
                return builder;
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun getHttpBase(token: String): OkHttpClient {
//            val builder = if (BuildConfig.DEBUG) {
//                getUnsafeOkHttpClientBuilder()
//            } else {
//                OkHttpClient.Builder()
//            }
            val builder = OkHttpClient.Builder()

            builder.addInterceptor {
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
            return builder.build()
        }

        fun getClient(baseUrl: String, token: String): Retrofit {
            val client = getHttpBase(token)

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