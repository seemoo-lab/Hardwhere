package com.heinecke.aron.LARS.data.model

import kotlinx.android.parcel.Parcelize

@Parcelize
data class Model (override val name: String, override val id: Int) : Selectable