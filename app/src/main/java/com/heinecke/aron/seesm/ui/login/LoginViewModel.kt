package com.heinecke.aron.seesm.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.seesm.data.Result

import com.heinecke.aron.seesm.R
import com.heinecke.aron.seesm.UnauthorizedException
import com.heinecke.aron.seesm.data.LoginDataSource

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(apiToken: String, endpoint: String) {
        // can be launched in a separate asynchronous job
        when(val result = LoginDataSource().login(apiToken, endpoint)) {
            is Result.Success -> _loginResult.value = LoginResult(success = true)
            is Result.Error -> {
                if ( result.exception is UnauthorizedException) {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                } else {
                    _loginResult.value = LoginResult(error = R.string.invalid_api_endpoint)
                }
            }
        }
    }

    fun loginDataChanged(endpoint: String, apiToken: String) {
        if (!isTokenValid(apiToken)) {
            _loginForm.value = LoginFormState(endpointError = R.string.invalid_token)
        } else if (!isEndpointValid(endpoint)) {
            _loginForm.value = LoginFormState(endpointError = R.string.invalid_api_endpoint)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A apiToken validation check
    private fun isTokenValid(apiToken: String): Boolean {
        return apiToken.isNotBlank()
    }

    // A endpoint validation check
    private fun isEndpointValid(endpoint: String): Boolean {
        return Patterns.WEB_URL.matcher(endpoint).matches()
    }

}
