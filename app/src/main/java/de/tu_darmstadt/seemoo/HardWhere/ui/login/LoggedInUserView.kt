package de.tu_darmstadt.seemoo.HardWhere.ui.login

import de.tu_darmstadt.seemoo.HardWhere.data.model.LoginData

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val displayName: String,
    val data: LoginData
)
