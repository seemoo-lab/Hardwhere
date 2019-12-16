package com.heinecke.aron.LARS.data.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class Manufacturer(override val id: Int, override val name: String) : Selectable