package com.heinecke.aron.LARS.data.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(override val id: Int, override val name: String) : Selectable