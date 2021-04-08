package de.tu_darmstadt.seemoo.LARS.ui.myassets

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
import de.tu_darmstadt.seemoo.LARS.ui.lib.ScanListViewModel

class MyCheckoutViewModel: ScanListViewModel() {
    val assetsToLent: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())


    /**
     * Last selected user for lenting
     */
    val lastSelectedUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)

    fun checkout(client: APIInterface, myUserId: Int) {
        incLoading()

        val requests: MutableList<Observable<Result<ResultAsset>>> = mutableListOf()
        requests.addAll(assetsToLent.value!!.map {
            client.checkout(it.createCheckout(myUserId))
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
                Log.d(this@MyCheckoutViewModel::class.java.name, "Finished with $it")
                processFinishedAssets(it, assetsToLent.value!!)
                decLoading()
            }) {
                Log.w(this@MyCheckoutViewModel::class.java.name, "Error: $it")
//                loading.postValue(
//                    Loading(
//                        it,
//                        false
//                    )
//                )
                decLoading()
            }
    }
}