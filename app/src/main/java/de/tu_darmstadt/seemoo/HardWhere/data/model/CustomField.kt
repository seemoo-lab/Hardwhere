package de.tu_darmstadt.seemoo.HardWhere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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