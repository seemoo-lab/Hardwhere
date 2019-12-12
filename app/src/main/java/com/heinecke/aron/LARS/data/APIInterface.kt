package com.heinecke.aron.LARS.data

import com.heinecke.aron.LARS.data.model.Asset
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("api/v1/hardware")
    fun getAsset(@Query("id") id: Int): Call<Asset>
}