package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Date(val datetime: String, val formatted: String) : Parcelable
