package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.heinecke.aron.LARS.data.model.Selectable.*

@Parcelize
data class Asset(var model: Model?, var category: Category?, var manufacturer: Manufacturer?,
                 var rtd_location: Location?, var updated_at: Date?,
                 var date_at: Date?, var available_actions: Actions?,
                 /** note that an invalid deserialization results in id = 0 **/
                 val id: Int = 0,
                 val name: String, val notes: String) : Parcelable {
    companion object {
        /**
         * Create a new empty asset
         * @param multiAsset Set true if multi-asset type is wanted, otherwise newAsset is set
         */
        fun getEmptyAsset(multiAsset: Boolean) : Asset {
            return Asset(null,null,null,null,null,null,null,if(multiAsset) ID_MULTI_ASSET else ID_NEW_ASSET,"","")
        }

        @JvmField val ID_MULTI_ASSET = -1
        @JvmField val ID_NEW_ASSET= 0

    }

    fun isMultiAsset() : Boolean {
        return id == ID_MULTI_ASSET
    }

    fun isNewAsset() : Boolean {
        return id == ID_NEW_ASSET
    }
}