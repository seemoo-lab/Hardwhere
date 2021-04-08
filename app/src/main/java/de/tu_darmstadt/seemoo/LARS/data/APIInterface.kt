package de.tu_darmstadt.seemoo.LARS.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.SearchResults
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*
import de.tu_darmstadt.seemoo.LARS.data.model.Result as Result1

interface APIInterface {
    @GET("api/v1/hardware/{id}")
    fun getAsset(@Path("id") id: Int): Call<Asset>

    @GET("api/v1/hardware/{id}")
    fun getAssetObservable(@Path("id") id: Int): Observable<Asset>

    @GET("api/v1/hardware")
    fun searchAsset(@Query("search") searchQuery: String): Call<SearchResults<Asset>>

    @GET("api/v1/users/{id}")
    fun getUserInfo(@Path("id") id: Int): Call<Selectable.User>

    @GET("api/v1/{type}")
    fun searchSelectable(@Path("type") type: String, @Query("search") searchQuery: String): Call<SearchResults<JsonElement>>

    @GET("api/v1/{type}/{id}")
    fun getSelectable(@Path("type") type: String, @Path("id") id: Int): Call<JsonElement>

    @GET("api/v1/{type}")
    fun getSelectablePage(@Path("type") type: String, @Query("limit") limit: Int, @Query("offset") offset: Int): Call<SearchResults<JsonElement>>

    @GET("LARS/api/checkedout")
    fun getLentAssets(): Call<ArrayList<Asset>>

    @GET("api/v1/users/{id}/assets")
    fun getCheckedoutAssets(@Path("id") id: Int): Call<SearchResults<Asset>>

    @POST("LARS/api/checkout")
    fun checkout(@Body data: JsonObject): Observable<Result1<Void>>

    @POST("LARS/api/checkin")
    fun checkin(@Body data: JsonObject): Observable<Result1<Void>>

    /**
     * @param data JsonElement of data to update, use makePath the Asset
     */
    @PATCH("api/v1/hardware/{id}")
    fun updateAsset(@Path("id") id: Int, @Body data: JsonObject): Observable<Result1<Asset>>

    companion object {
        fun makeAssetPatch(data: Any): JsonElement {
            val gson = Gson()
            val jsonelem = gson.toJsonTree(data)
            jsonelem.asJsonObject.entrySet().removeIf {
                it.value.isJsonNull
            }

            return jsonelem
        }
    }

}