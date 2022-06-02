package de.tu_darmstadt.seemoo.HardWhere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Asset Model
 */
@Parcelize
data class Model(
    val id: Int,
    val name: String,
    val assets_count: Int,
    val fieldset: Selectable.FieldsetShort?,
    val notes: String,
): Parcelable
