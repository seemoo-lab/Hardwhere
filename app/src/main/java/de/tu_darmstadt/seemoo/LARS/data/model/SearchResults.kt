package de.tu_darmstadt.seemoo.LARS.data.model

data class SearchResults<T>(val total: Int, val rows: ArrayList<T>)