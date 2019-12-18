package com.heinecke.aron.LARS.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.SearchResults
import com.heinecke.aron.LARS.data.model.Selectable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*
import com.heinecke.aron.LARS.data.model.Result as Result1

interface APIInterface {
    @GET("api/v1/hardware/{id}")
    fun getAsset(@Path("id") id: Int): Call<Asset>

    @GET("api/v1/users/{id}")
    fun getUserInfo(@Path("id") id: Int): Call<Selectable.User>

    @GET("api/v1/{type}")
    fun searchSelectable(@Path("type") type: String, @Query("search") searchQuery: String): Call<SearchResults>

    @GET("api/v1/{type}/{id}")
    fun getSelectable(@Path("type") type: String, @Path("id") id: Int): Call<JsonElement>

    @GET("api/v1/{type}")
    fun getSelectablePage(@Path("type") type: String, @Query("limit") limit: Int, @Query("offset") offset: Int): Call<SearchResults>

    /**
     * @param data JsonElement of data to update, use makePath the Asset
     */
    @PATCH("api/v1/asset/{id}")
    fun updateAsset(@Path("id") id: Int, @Body data: JsonElement) : Observable<Result1>

    companion object {
        fun makeAssetPatch(data: Any) : JsonElement {
            val gson = Gson()
            val jsonelem = gson.toJsonTree(data)
            jsonelem.asJsonObject.entrySet().removeIf {
                it.value.isJsonNull
            }

            return jsonelem
        }
    }
}