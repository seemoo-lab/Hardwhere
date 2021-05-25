package de.tu_darmstadt.seemoo.HardWhere.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null,
    val errorDetail: Exception? = null
)
