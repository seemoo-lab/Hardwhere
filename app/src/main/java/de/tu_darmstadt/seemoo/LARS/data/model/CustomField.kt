package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomField(
    /**
     * Internal unique ID
     */
    var field: String,
    var value:String?,
    /**
     * "ANY" for anything
     */
    var field_format: String
) : Parcelable