package com.heinecke.aron.seesm.data.model

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("uid")
    val userID: Int,
    @SerializedName("token")
    val apiToken: String,
    @SerializedName("url")
    val apiSource: String )