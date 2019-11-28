package com.heinecke.aron.seesm

data class InvalidResponseException(val status: Int, val body: String) : Exception()

class UnauthorizedException() : Exception()