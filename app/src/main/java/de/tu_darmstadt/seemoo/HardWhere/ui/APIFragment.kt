package de.tu_darmstadt.seemoo.HardWhere.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.HardWhere.MainViewModel
import de.tu_darmstadt.seemoo.HardWhere.data.APIClient
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.LoginData

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

    protected fun getUserID(): Int = mainViewModel.requireLoginData(requireContext()).userID

    /**
     * Set fragment title for custom overrides
     */
    protected fun title(title: String) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }
}