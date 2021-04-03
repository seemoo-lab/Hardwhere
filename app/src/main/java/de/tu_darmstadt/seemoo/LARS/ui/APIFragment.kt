package de.tu_darmstadt.seemoo.LARS.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.MainViewModel
import de.tu_darmstadt.seemoo.LARS.data.APIClient
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.LoginData

/**
 * Base fragment with API access, contains getAPI() function with cached API access and credentials
 */
abstract class APIFragment : Fragment() {
    protected lateinit var mainViewModel: MainViewModel
    private lateinit var api: APIInterface

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Requires Activity!")

        mainViewModel.loginData.observe(this, Observer {
            it?.run { updateAPI(this) }
        })
        mainViewModel.loginData.value?.run {
            updateAPI(this)
        }
    }

    private fun updateAPI(data: LoginData?) {
        val login = data ?: mainViewModel.requireLoginData(requireContext())
        val client = APIClient.getClient(login.apiBackend, login.apiToken)
        api = client.create(APIInterface::class.java)
    }

    protected fun getAPI(): APIInterface {
        if (!this::api.isInitialized){
            val login = mainViewModel.requireLoginData(requireContext());
            val client = APIClient.getClient(login.apiBackend, login.apiToken)
            api = client.create(APIInterface::class.java)
        }
        return api
    }
}