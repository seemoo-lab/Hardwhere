package de.tu_darmstadt.seemoo.LARS.ui.ownassets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.SearchResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OwnAssetsViewModel : ViewModel() {
    private val _checkedOutAsssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    val checkedOutAsset: LiveData<ArrayList<Asset>> = _checkedOutAsssets
    private val _loading: MutableLiveData<Int> = MutableLiveData(0)
    val loading: LiveData<Int> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    fun loadData(client: APIInterface, userID: Int) {
        incLoading()
        client.getCheckedoutAssets(userID).enqueue(object : Callback<SearchResults<Asset>> {
            override fun onResponse(call: Call<SearchResults<Asset>>, response: Response<SearchResults<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assets = this.body()!!.rows // TODO: iterate to load all of them if required!
                        _checkedOutAsssets.value!!.clear()
                        _checkedOutAsssets.value!!.addAll(assets)
                        log = false
                    } else {
                        _error.value = "Failed to load checked out assets!"
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@OwnAssetsViewModel::class.java,
                        response
                    )
                decLoading()
            }

            override fun onFailure(call: Call<SearchResults<Asset>>, t: Throwable) {
                _error.value = "Failed to load checked out assets!"
                decLoading()
            }

        })
    }

    internal fun decLoading() {
        val value = _loading.value!!
        val newValue = if (value > 0)
            value - 1
        else
            0
        _loading.postValue(newValue)
    }

    internal fun incLoading() {
        _loading.postValue(_loading.value!! + 1)
    }
}