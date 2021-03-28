package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.BuildConfig
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import de.tu_darmstadt.seemoo.LARS.data.model.Result

class CheckinViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Large Accessories Retrieval System\nBuild: ${BuildConfig.BUILD_TIME} \nCommit: ${BuildConfig.GitHash}"
    }
    val text: LiveData<String> = _text

    private val _checkedOutAsssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    val checkedOutAsset: LiveData<ArrayList<Asset>> = _checkedOutAsssets
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    fun loadData(client: APIInterface) {
        _loading.value = true
        client.getCheckedOutAssets().enqueue(object : Callback<ArrayList<Asset>> {
            override fun onResponse(call: Call<ArrayList<Asset>>, response: Response<ArrayList<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        _checkedOutAsssets.value!!.clear()
                        _checkedOutAsssets.value!!.addAll(this.body()!!)
                        log = false
                    } else {
                        _error.value = "Failed to load checked out assets!"
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@CheckinViewModel::class.java,
                        response
                    )
                _loading.value = false
            }

            override fun onFailure(call: Call<ArrayList<Asset>>, t: Throwable) {
                _error.value = "Failed to load checked out assets!"
                _loading.value = false
            }

        })
    }

    fun checkin(client: APIInterface, assets: ArrayList<Asset>) {
        _loading.value = true

        val requests: MutableList<Observable<Result<Void>>> = mutableListOf()
        requests.addAll(assets.map {
            client.checkin()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
        })

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val failed = list.filter {
                if (it is Result<*>) {
                    if (it.status == "success") {
                        return@filter false
                    }
                }
                true
            }
            failed
        }
            .subscribe({
                Log.d(this@CheckinViewModel::class.java.name, "Finished with $it")
                loading.postValue(
                    Loading(
                        null,
                        it.isEmpty()
                    )
                )
                editingFinished.postValue(1)
            }) {
                Log.w(this@CheckinViewModel::class.java.name, "Error: $it")
                loading.postValue(
                    Loading(
                        it,
                        false
                    )
                )
            }
    }
}