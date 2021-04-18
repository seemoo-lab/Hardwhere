package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import io.sentry.core.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LentViewModel : ViewModel() {
    private val _checkedOutAsssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    val checkedOutAsset: LiveData<ArrayList<Asset>> = _checkedOutAsssets
    val filteredAssets: MutableLiveData<List<Asset>> = MutableLiveData(listOf())
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error
    /**
     * User to filter after if set
     */
    internal val filteredUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)


    fun resetError() {
        _error.value = null
    }

    fun loadData(client: APIInterface, userID: Int) {
        _loading.value = true
        client.getLentAssets().enqueue(object : Callback<ArrayList<Asset>> {
            override fun onResponse(call: Call<ArrayList<Asset>>, response: Response<ArrayList<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assetsLent = this.body()!!.filter { a ->
                            if (a.assigned_to != null) {
                                a.assigned_to.id != userID
                            } else {
                                Log.w(this@LentViewModel::class.java.name,"Received asset that has no assigned_to! $a")
                                false
                            }
                        }
                        _checkedOutAsssets.value!!.clear()
                        _checkedOutAsssets.value!!.addAll(assetsLent)
                        log = false
                    } else {
                        _error.value = "Failed to load checked out assets!"
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@LentViewModel::class.java,
                        response
                    )
                _loading.value = false
            }

            override fun onFailure(call: Call<ArrayList<Asset>>, t: Throwable) {
                _error.value = "Failed to load checked out assets!"
                _loading.value = false
                Sentry.captureException(t)
            }

        })
    }


}