package com.heinecke.aron.LARS.data.model

import com.google.gson.JsonElement

/**
 * Result for post/patch where no data is returned
 */
data class Result(val status: String, val messages: String, val payload: JsonElement?)