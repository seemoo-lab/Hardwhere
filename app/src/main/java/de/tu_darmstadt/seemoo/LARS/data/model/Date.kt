package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Date(val datetime: String, val formatted: String) : Parcelable