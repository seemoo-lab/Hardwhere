package com.heinecke.aron.LARS.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.heinecke.aron.LARS.MainViewModel
import com.heinecke.aron.LARS.data.APIClient
import com.heinecke.aron.LARS.data.APIInterface
import com.heinecke.aron.LARS.data.model.LoginData

abstract class APIFragment : Fragment() {
    protected lateinit var mainViewModel: MainViewModel
    private lateinit var api: APIInterface

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = activity?.run {
            ViewModelProviders.of(requireActivity())[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        mainViewModel.loginData.observe(this, Observer {
            updateAPI(it)
        })
        updateAPI(mainViewModel.loginData.value)
    }

    private fun updateAPI(data: LoginData?) {
        val login = data ?: mainViewModel.getLoginData(requireContext())
        val client = APIClient.getClient(login.apiBackend, login.apiToken)
        api = client.create(APIInterface::class.java)
    }

    protected fun getAPI(): APIInterface {
        return api
    }
}