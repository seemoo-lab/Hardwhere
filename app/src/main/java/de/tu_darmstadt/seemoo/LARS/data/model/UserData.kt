package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(val name: String, val email: String) : Parcelable