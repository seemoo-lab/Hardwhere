package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.android.parcel.Parcelize

/**
 * Interface for models that have a name & id, used for selection dialogs
 */
interface Selectable : Parcelable {
    val name: String
    val id: Int

    @Parcelize
    enum class SelectableType : Parcelable {
        Model {
            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Model>(input, Selectable.Model::class.java)
            }

            override fun getTypeName(): String {
                return "models"
            }
        },
        User {
            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.User>(input, Selectable.User::class.java)
            }

            override fun getTypeName(): String {
                return "users"
            }
        },
        Manufacturer {
            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Manufacturer>(
                    input,
                    Selectable.Manufacturer::class.java
                )
            }

            override fun getTypeName(): String {
                return "manufacturers"
            }
        },
        Location {
            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Location>(input, Selectable.Location::class.java)
            }

            override fun getTypeName(): String {
                return "locations"
            }
        },
        Category {
            override fun parseElement(input: JsonElement): Selectable {
                return Gson().fromJson<Selectable.Category>(input, Selectable.Category::class.java)
            }

            override fun getTypeName(): String {
                return "categories"
            }
        };

        abstract fun getTypeName(): String
        abstract fun parseElement(input: JsonElement): Selectable
    }

    // we can't do @EqualsAndHasCode(of="index") and can't implement it in the interface
    // thus we have to do the plumbing here
    @Parcelize
    data class Model(override val id: Int, override val name: String) : Selectable {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as Selectable
            if (id != other.id) {
                return false
            }
            return true
        }
    }

    @Parcelize
    data class User(override var id: Int, override var name: String, var email: String) : Selectable {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as Selectable
            if (id != other.id) {
                return false
            }
            return true
        }
    }

    @Parcelize
    data class Manufacturer(override val id: Int, override val name: String) : Selectable {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as Selectable
            if (id != other.id) {
                return false
            }
            return true
        }
    }

    @Parcelize
    data class Location(override val id: Int, override val name: String) : Selectable {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as Selectable
            if (id != other.id) {
                return false
            }
            return true
        }
    }

    @Parcelize
    data class Category(override val id: Int, override val name: String) : Selectable {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as Selectable
            if (id != other.id) {
                return false
            }
            return true
        }
    }
}