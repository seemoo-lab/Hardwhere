package com.heinecke.aron.LARS.data.model

data class SearchResults<T>(val total: Int, val rows: List<T>)