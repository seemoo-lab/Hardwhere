package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.heinecke.aron.LARS.data.model.Selectable.*
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
data class Asset(
    var model: Model?, var category: Category?,
    /**
     * Non editable, comes from model
     */
    val manufacturer: Manufacturer?,
    var rtd_location: Location?, var updated_at: Date?,
    var date_at: Date?, var available_actions: Actions?,
    /** note that an invalid deserialization results in id = 0 **/
    val id: Int = 0,
    val name: String, val notes: String,
    @Transient
    val selected: Boolean = false
) : Parcelable {

    companion object {
        /**
         * Create a new empty asset
         * @param multiAsset Set true if multi-asset type is wanted, otherwise newAsset is set
         */
        fun getEmptyAsset(multiAsset: Boolean): Asset {
            return Asset(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                if (multiAsset) ID_MULTI_ASSET else ID_NEW_ASSET,
                "",
                ""
            )
        }

        @JvmField
        val ID_MULTI_ASSET = -1
        @JvmField
        val ID_NEW_ASSET = 0

    }

    fun createPatch() : JsonObject {
        val base = JsonObject()
        this.model?.run { base.addProperty("model_id",this.id) }
        this.category?.run { base.addProperty("category",this.id) }
        this.rtd_location?.run { base.addProperty("rtd_location_id",this.id) }
        @Suppress("UNNECESSARY_SAFE_CALL")
        this.name?.run { base.addProperty("name",this) }
        @Suppress("UNNECESSARY_SAFE_CALL")
        this.notes?.run { base.addProperty("notes",this) }

        return base
    }

    fun isMultiAsset(): Boolean {
        return id == ID_MULTI_ASSET
    }

    fun isNewAsset(): Boolean {
        return id == ID_NEW_ASSET
    }
}