package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginData(
    @SerializedName("uid")
    val userID: Int,
    @SerializedName("token")
    val apiToken: String,
    @SerializedName("url")
    val apiBackend: String
) : Parcelable