package de.tu_darmstadt.seemoo.LARS.ui.lent

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
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable

class LentViewModel : ViewModel() {
    private val _checkedOutAsssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    val checkedOutAsset: LiveData<ArrayList<Asset>> = _checkedOutAsssets
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    /**
     * Last selected user for lenting
     */
    val lastSelectedUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)

    /**
     * Notifies of finished lenting
     */
    private val _lentFinished: MutableLiveData<Boolean> = MutableLiveData(false)
    val lentFinished: LiveData<Boolean> = _lentFinished

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
                        this@LentViewModel::class.java,
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

    /**
     * Reset loading finished
     */
    fun resetFinishedLoading() {
        _lentFinished.value = false
    }

    fun checkout(client: APIInterface, assets: ArrayList<Asset>, user: Int) {
        _loading.value = true
        _lentFinished.value = false

        val requests: MutableList<Observable<Result<Void>>> = mutableListOf()
        requests.addAll(assets.map {
            client.checkout(it.createCheckout(user))
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
                Log.d(this@LentViewModel::class.java.name, "Finished with $it")
//                loading.postValue(
//                    Loading(
//                        null,
//                        it.isEmpty()
//                    )
//                )
                _lentFinished.value = true
                _loading.value = false
            }) {
                Log.w(this@LentViewModel::class.java.name, "Error: $it")
//                loading.postValue(
//                    Loading(
//                        it,
//                        false
//                    )
//                )
                _loading.value = false
            }
    }
}