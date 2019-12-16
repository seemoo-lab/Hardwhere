package com.heinecke.aron.LARS.data.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(override var name: String, var email: String, override var id: Int) : Selectable