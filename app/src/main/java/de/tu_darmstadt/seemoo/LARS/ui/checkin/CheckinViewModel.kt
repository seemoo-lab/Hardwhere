package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.collections.ArrayList
import de.tu_darmstadt.seemoo.LARS.data.model.Result
import de.tu_darmstadt.seemoo.LARS.data.model.ResultAsset

class CheckinViewModel: ViewModel() {
    val assetsToReturn: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())

    private val _loading: MutableLiveData<Int> = MutableLiveData(0)
    val loading: LiveData<Int> = _loading

    private val _finishedAssets: MutableLiveData<List<Asset>?> = MutableLiveData()

    /**
     * Assets that finished checkin, to be removed on main thread
     */
    val finishedAssets: LiveData<List<Asset>?> = _finishedAssets
    /**
     * Last selected user for lenting
     */
    val lastSelectedUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)

    fun checkin(client: APIInterface) {
        incLoading()
        resetFinishedAssets()

        val requests: MutableList<Observable<Result<ResultAsset>>> = mutableListOf()
        requests.addAll(assetsToReturn.value!!.map {
            client.checkin(it.createCheckin())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
        })

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val failed = list.filter {
                if (it is Result<*>) {
                    if (it.status == "success") {
                        return@filter true
                    }
                }
                false
            }
            failed
        }
            .subscribe({
                Log.d(this@CheckinViewModel::class.java.name, "Finished with $it")
                val finished = it.mapNotNull {
                    val item = it as Result<ResultAsset>
                    assetsToReturn.value!!.find {
                        it.asset_tag == item.payload!!.asset
                    }
                }
                _finishedAssets.postValue(finished)
                decLoading()
            }) {
                Log.w(this@CheckinViewModel::class.java.name, "Error: $it")
//                loading.postValue(
//                    Loading(
//                        it,
//                        false
//                    )
//                )
                decLoading()
            }
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

    fun resetFinishedAssets() {
        _finishedAssets.value = null
    }
}