package de.tu_darmstadt.seemoo.LARS.ui.lent

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

class LentingViewModel: ViewModel() {
    val assetsToLent: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())

    private val _loading: MutableLiveData<Int> = MutableLiveData(0)
    val loading: LiveData<Int> = _loading
    /**
     * Last selected user for lenting
     */
    val lastSelectedUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)

    /**
     * Notifies of finished lenting
     */
    private val _lentFinished: MutableLiveData<Boolean> = MutableLiveData(false)
    val lentFinished: LiveData<Boolean> = _lentFinished

    /**
     * Reset loading finished
     */
    fun resetFinishedLoading() {
        _lentFinished.value = false
    }

    fun checkout(client: APIInterface) {
        incLoading()
        _lentFinished.value = false

        val requests: MutableList<Observable<Result<Void>>> = mutableListOf()
        requests.addAll(assetsToLent.value!!.map {
            client.checkout(it.createCheckout(lastSelectedUser.value!!.id))
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
                Log.d(this@LentingViewModel::class.java.name, "Finished with $it")
//                loading.postValue(
//                    Loading(
//                        null,
//                        it.isEmpty()
//                    )
//                )
                _lentFinished.postValue(true)
                decLoading()
            }) {
                Log.w(this@LentingViewModel::class.java.name, "Error: $it")
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
}