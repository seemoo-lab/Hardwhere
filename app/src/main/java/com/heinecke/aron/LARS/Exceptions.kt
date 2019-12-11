package com.heinecke.aron.LARS

data class InvalidResponseException(val status: Int, val body: String) : Exception()

class UnauthorizedException() : Exception()

class InvalidUserIDException() : Exception()