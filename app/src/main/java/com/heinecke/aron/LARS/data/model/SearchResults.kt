package com.heinecke.aron.LARS.data.model

import com.google.gson.JsonElement

data class SearchResults(val total: Int, val rows: List<JsonElement>)