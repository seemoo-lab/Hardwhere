package de.tu_darmstadt.seemoo.HardWhere.ui.lent

import android.util.Log
import androidx.lifecycle.MutableLiveData
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import de.tu_darmstadt.seemoo.HardWhere.data.model.Result
import de.tu_darmstadt.seemoo.HardWhere.data.model.ResultAsset
import de.tu_darmstadt.seemoo.HardWhere.ui.lib.ScanListViewModel
import io.sentry.core.Sentry

class LentingViewModel: ScanListViewModel() {
    /**
     * Last selected user for lenting
     */
    val lastSelectedUser: MutableLiveData<Selectable.User?> = MutableLiveData(null)


    fun checkout(client: APIInterface) {
        if(assetList.value!!.isNotEmpty())
            incLoading()

        val requests: MutableList<Observable<Result<ResultAsset>>> = mutableListOf()
        requests.addAll(assetList.value!!.map {
            client.checkout(it.createCheckout(lastSelectedUser.value!!.id))
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
                Log.d(this@LentingViewModel::class.java.name, "Finished with $it")
                processFinishedAssets(it, assetList.value!!)
                decLoading()
            }) {
                Log.w(this@LentingViewModel::class.java.name, "Error: $it")
                _error.postValue(Pair(R.string.error_checkout_assets,it))
                Sentry.captureException(it)
                decLoading()
            }
    }
}