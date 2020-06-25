package de.tu_darmstadt.seemoo.LARS.data.model

/**
 * Result for post/patch where no data is returned
 */
data class Result<T>(val status: String, val messages: String, val payload: T?)