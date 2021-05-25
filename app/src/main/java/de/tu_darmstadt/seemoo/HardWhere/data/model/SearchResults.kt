package de.tu_darmstadt.seemoo.HardWhere.data.model

data class SearchResults<T>(val total: Int, val rows: ArrayList<T>)