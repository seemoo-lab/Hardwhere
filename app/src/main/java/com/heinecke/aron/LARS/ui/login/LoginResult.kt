package com.heinecke.aron.LARS.ui.login

import java.lang.Exception

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
        val success: LoggedInUserView? = null,
        val error: Int? = null,
        val errorDetail: Exception? = null
)
