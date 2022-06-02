package de.tu_darmstadt.seemoo.HardWhere.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FieldSet (
    val id: Int,
    val name: String?,
    val fields: Fields?
): Parcelable {
    @Parcelize
    class CustomFieldDefinition (
        val id: Int,
        val name: String,
        val db_column_name: String,
        // ONLY for fieldset reported fields! https://github.com/snipe/snipe-it/issues/11093
        val type: String?,
        /**
         * null when type is text etc
          */
        val field_values_array: List<String>?,
        val required: Boolean
    ): Parcelable

    @Parcelize
    data class Fields (
        val total: Int,
        /**
         * int-key doesn't have any relevance
         */
        val rows: HashMap<Int,CustomFieldDefinition>
    ): Parcelable
}