package com.heinecke.aron.LARS.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val endpointError: Int? = null,
    val tokenError: Int? = null,
    val userIDError: Int? = null,
    val isDataValid: Boolean = false
)
