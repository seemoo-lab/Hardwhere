package com.heinecke.aron.LARS.data

import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIInterface {
    @GET("api/v1/hardware/{id}")
    fun getAsset(@Path("id") id: Int): Call<Asset>


    @GET("api/v1/user/{id}")
    fun getUserInfo(@Path("id") id: Int): Call<User>
}