package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Date(val datetime: String, val formatted: String) : Parcelable
