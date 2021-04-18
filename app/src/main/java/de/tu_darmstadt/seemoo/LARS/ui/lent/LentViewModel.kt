package de.tu_darmstadt.seemoo.LARS.ui.lent

import android.util.Log
import androidx.lifecycle.*
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import io.sentry.core.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LentViewModel : ViewModel() {
    private val _checkedOutAsssets: MutableLiveData<List<Asset>> = MutableLiveData(listOf())
    val checkedOutAsset: LiveData<List<Asset>> = _checkedOutAsssets
    /**
     * User to filter after if set
     */
    internal val filteredUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)
    val filteredAssets: LiveData<List<Asset>> = MediatorLiveData<List<Asset>>().apply {
        fun update() {
            val user = filteredUser.value
            val list = checkedOutAsset.value
            if (list == null) {
                value = listOf()
                return
            }
            if (user == null) {
                value = list
            } else {
                value = list.filter { asset ->
                    if (asset.assigned_to != null) {
                        asset.assigned_to.id == user.id
                    } else {
                        Log.w(this::class.java.name, "Asset without assigned user! ${asset.asset_tag}")
                        true
                    }
                }
            }
        }

        addSource(filteredUser) {update()}
        addSource(checkedOutAsset) { update()}
        update()
    }
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error


    fun updateAsset(assets: ArrayList<Asset>): List<Asset> {
        val filterUser = filteredUser.value
        return if (filterUser == null) {
            assets
        } else {
            assets.filter { asset ->
                if (asset.assigned_to != null) {
                    asset.assigned_to.id == filterUser.id
                } else {
                    Log.w(this::class.java.name, "Asset without assigned user! ${asset.asset_tag}")
                    true
                }
            }
        }
    }

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
                        _checkedOutAsssets.value = assetsLent
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