package com.heinecke.aron.seesm.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(val endpointError: Int? = null,
                          val userIDError: Int? = null,
                          val isDataValid: Boolean = false)
