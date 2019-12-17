package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.heinecke.aron.LARS.data.APIInterface
import kotlinx.android.parcel.Parcelize
import retrofit2.Call

/**
 * Interface for models that have a name & id, used for selection dialogs
 */
interface Selectable : Parcelable {
    val name: String
    val id: Int

    @Parcelize
    enum class SelectableType : Parcelable {
        Model {
            override fun getSelectable(id: Int, client: APIInterface): Call<JsonElement> {
                return client.getSelectable("models", id)
            }

            override fun searchSelectable(
                query: String,
                client: APIInterface
            ): Call<SearchResults> {
                return client.searchSelectable("models", query)
            }

            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Model>(input, Selectable.Model::class.java)
            }
        },
        User {
            override fun getSelectable(id: Int, client: APIInterface): Call<JsonElement> {
                return client.getSelectable("user", id)
            }

            override fun searchSelectable(
                query: String,
                client: APIInterface
            ): Call<SearchResults> {
                return client.searchSelectable("user", query)
            }

            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.User>(input, Selectable.User::class.java)
            }
        },
        //        Date,
        Manufacturer {
            override fun getSelectable(id: Int, client: APIInterface): Call<JsonElement> {
                return client.getSelectable("manufacturers", id)
            }

            override fun searchSelectable(
                query: String,
                client: APIInterface
            ): Call<SearchResults> {
                return client.searchSelectable("manufacturers", query)
            }

            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Manufacturer>(
                    input,
                    Selectable.Manufacturer::class.java
                )
            }
        },
        Location {
            override fun getSelectable(id: Int, client: APIInterface): Call<JsonElement> {
                return client.getSelectable("locations", id)
            }

            override fun searchSelectable(
                query: String,
                client: APIInterface
            ): Call<SearchResults> {
                return client.searchSelectable("locations", query)
            }

            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Location>(input, Selectable.Location::class.java)
            }
        },
        Category {
            override fun getSelectable(id: Int, client: APIInterface): Call<JsonElement> {
                return client.getSelectable("categories", id)
            }

            override fun searchSelectable(
                query: String,
                client: APIInterface
            ): Call<SearchResults> {
                return client.searchSelectable("categories", query)
            }

            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Category>(input, Selectable.Category::class.java)
            }
        };

        abstract fun getSelectable(id: Int, client: APIInterface): Call<JsonElement>
        abstract fun searchSelectable(query: String, client: APIInterface): Call<SearchResults>
        abstract fun parseElement(input: JsonElement): Selectable
    }

    @Parcelize
    data class Model(override val name: String, override val id: Int) : Selectable

    @Parcelize
    data class User(override var name: String, var email: String, override var id: Int) : Selectable

    @Parcelize
    data class Date(val datetime: String, val formatted: String) : Parcelable

    @Parcelize
    data class Manufacturer(override val id: Int, override val name: String) : Selectable

    @Parcelize
    data class Location(override val id: Int, override val name: String) : Selectable

    @Parcelize
    data class Category(override val id: Int, override val name: String) : Selectable
}