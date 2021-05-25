package de.tu_darmstadt.seemoo.HardWhere.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.HardWhere.InvalidResponseException
import de.tu_darmstadt.seemoo.HardWhere.InvalidUserIDException
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.UnauthorizedException
import de.tu_darmstadt.seemoo.HardWhere.data.LoginDataSource
import de.tu_darmstadt.seemoo.HardWhere.data.Result
import java.net.UnknownHostException

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm


    private val backgroundResult = MutableLiveData<Result<LoggedInUserView>>()

    val loginResult: LiveData<LoginResult> = Transformations.map(backgroundResult) { res ->
        when (res) {
            is Result.Success -> LoginResult(res.data)
            is Result.Error -> {
                when (res.exception) {
                    is UnauthorizedException -> LoginResult(error = R.string.login_failed)
                    is InvalidResponseException -> LoginResult(error = R.string.invalid_api_endpoint)
                    is UnknownHostException -> LoginResult(error = R.string.error_unknown_host)
                    is InvalidUserIDException -> LoginResult(error = R.string.invalid_user_id)
                    else -> { // TODO: when throw invalid_connection ?
                        LoginResult(
                            error = R.string.invalid_api_endpoint,
                            errorDetail = res.exception
                        )
                    }
                }
            }
        }
    }

    fun login(apiToken: String, endpoint: String) {
        // can be launched in a separate asynchronous job
        LoginDataSource().login(endpoint, apiToken, backgroundResult)
    }

    fun loginDataChanged(endpoint: String, apiToken: String) {
        if (!isTokenValid(apiToken)) {
            _loginForm.value = LoginFormState(tokenError = R.string.invalid_token)
        } else if (!isEndpointValid(endpoint)) {
            _loginForm.value = LoginFormState(endpointError = R.string.invalid_api_endpoint)
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
