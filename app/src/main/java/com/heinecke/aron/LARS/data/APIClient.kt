package com.heinecke.aron.LARS.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClient {
    companion object {
        fun getClient(baseUrl: String, token: String): Retrofit {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.NONE

            val client: OkHttpClient =
                OkHttpClient.Builder().addInterceptor {
                    val newRequest: Request = it.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    it.proceed(newRequest)
                }.addInterceptor(interceptor).build()

            val retrofit: Retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit
        }
    }
}