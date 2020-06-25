package de.tu_darmstadt.seemoo.LARS.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.InvalidResponseException
import de.tu_darmstadt.seemoo.LARS.InvalidUserIDException
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.UnauthorizedException
import de.tu_darmstadt.seemoo.LARS.data.LoginDataSource
import de.tu_darmstadt.seemoo.LARS.data.Result
import de.tu_darmstadt.seemoo.LARS.data.model.LoginData
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

    fun login(apiToken: String, endpoint: String, userID: Int) {
        // can be launched in a separate asynchronous job
        LoginDataSource().login(LoginData(userID, apiToken, endpoint), backgroundResult)
    }

    fun loginDataChanged(endpoint: String, apiToken: String, userID: String) {
        if (!isTokenValid(apiToken)) {
            _loginForm.value = LoginFormState(tokenError = R.string.invalid_token)
        } else if (!isEndpointValid(endpoint)) {
            _loginForm.value = LoginFormState(endpointError = R.string.invalid_api_endpoint)
        } else {
            try {
                val uid = Integer.parseInt(userID)
                if (uid < 1) {
                    _loginForm.value = LoginFormState(userIDError = R.string.invalid_user_id)
                }

                _loginForm.value = LoginFormState(isDataValid = true)
            } catch (e: NumberFormatException) {
                _loginForm.value = LoginFormState(userIDError = R.string.invalid_user_id)
            }
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
