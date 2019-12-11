package com.heinecke.aron.LARS.ui.login

import com.heinecke.aron.LARS.data.model.LoginData

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
        val displayName: String,
        val data: LoginData
)
