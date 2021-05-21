package de.tu_darmstadt.seemoo.LARS.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.zxing.integration.android.IntentIntegrator
import de.tu_darmstadt.seemoo.LARS.MainActivity
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.model.LoginData
import de.tu_darmstadt.seemoo.LARS.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            binding.login.isEnabled = loginState.isDataValid

            if (loginState.endpointError != null) {
                binding.apiEndpoint.error = getString(loginState.endpointError)
            }

            if (loginState.tokenError != null) {
                binding.apiToken.error = getString(loginState.tokenError)
            }

        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            binding.loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error, loginResult.errorDetail)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)

                val prefs = getSharedPreferences(Utils.PREFS_APP, 0).edit()
                prefs.putBoolean(Utils.PREFS_KEY_FIRST_RUN, false)
                val data = loginResult.success.data
                prefs.putInt(Utils.PREFS_KEY_UID, data.userID)
                prefs.putString(Utils.PREFS_KEY_BACKEND, data.apiBackend)
                prefs.putString(Utils.PREFS_KEY_TOKEN, data.apiToken)
                prefs.apply()

                //Complete and destroy login activity once successful
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        })

        binding.scanLogin.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan QR-Login Code")

            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.initiateScan()
        }

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            loginViewModel.login(
                binding.apiToken.text.toString(),
                binding.apiEndpoint.text.toString()
            )
        }

        binding.apiEndpoint.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    binding.apiEndpoint.text.toString(),
                    binding.apiToken.text.toString()
                )
            }
        }

        binding.apiToken.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    binding.apiEndpoint.text.toString(),
                    binding.apiToken.text.toString()
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val gson = Gson()
                try {
                    val loginData: LoginData = gson.fromJson(result.contents, LoginData::class.java)
                    Log.d(this::class.java.name, "Data: ${loginData.apiToken}")
                    binding.apiToken.setText(loginData.apiToken)
                    binding.apiEndpoint.setText(loginData.apiBackend)
                } catch (e: JsonSyntaxException) {
                    Toast.makeText(this, R.string.invalid_login_json, Toast.LENGTH_LONG).show()
                    Log.d(this::class.java.name, "Can't parse login json", e)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int, details: Exception?) {
        val msg = details ?: "No details."
        Toast.makeText(applicationContext, "${getString(errorString)} $msg", Toast.LENGTH_LONG)
            .show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
