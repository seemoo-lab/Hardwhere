package com.heinecke.aron.seesm.ui.login

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.heinecke.aron.seesm.R


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val apiToken = findViewById<EditText>(R.id.apiToken)
        val apiEndpoint = findViewById<EditText>(R.id.apiEndpoint)
        val userID = findViewById<EditText>(R.id.userID)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val scanLogin = findViewById<Button>(R.id.scanLogin)

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.endpointError != null) {
                apiToken.error = getString(loginState.endpointError)
            }

            if (loginState.userIDError != null) {
                userID.error = getString(loginState.userIDError)
            }

        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//            if(result != null) {
//                if(result.getContents() == null) {
//                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
//                }
//            } else {
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        }

        scanLogin.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan a QR-Login Code")

            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.initiateScan()
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            loginViewModel.login(apiEndpoint.text.toString(),apiToken.text.toString(),Integer.parseInt(userID.text.toString()))
        }
        apiToken.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        apiEndpoint.text.toString(),
                        apiToken.text.toString(),
                        userID.text.toString()
                )
            }
        }

        userID.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    apiEndpoint.text.toString(),
                    apiToken.text.toString(),
                    userID.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            apiEndpoint.text.toString(),
                            apiToken.text.toString(),
                            Integer.parseInt(userID.text.toString())
                        )
                }
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)

        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
                applicationContext,
                "$welcome $displayName",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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
