package de.tu_darmstadt.seemoo.HardWhere

data class InvalidResponseException(val status: Int, val body: String) : Exception()

class UnauthorizedException() : Exception()

class InvalidUserIDException() : Exception()