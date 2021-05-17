package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actions(
    val checkout: Boolean,
    val checkin: Boolean,
    val update: Boolean,
    val delete: Boolean
) :
    Parcelable