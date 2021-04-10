package de.tu_darmstadt.seemoo.LARS.ui.checkin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.collections.ArrayList
import de.tu_darmstadt.seemoo.LARS.data.model.Result
import de.tu_darmstadt.seemoo.LARS.data.model.ResultAsset
import de.tu_darmstadt.seemoo.LARS.ui.lib.ScanListViewModel
import io.sentry.core.Sentry

class CheckinViewModel: ScanListViewModel() {
    val assetsToReturn: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
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
                processFinishedAssets(it,assetsToReturn.value!!)
                decLoading()
            }) {
                Log.w(this@CheckinViewModel::class.java.name, "Error: $it")
                _error.postValue(Pair(R.string.error_checkin_assets,it))
                Sentry.captureException(it)
                decLoading()
            }
    }
}