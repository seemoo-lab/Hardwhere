package com.heinecke.aron.LARS.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Actions(val checkout: Boolean, val checkin: Boolean, val update: Boolean, val delete: Boolean) :
    Parcelable