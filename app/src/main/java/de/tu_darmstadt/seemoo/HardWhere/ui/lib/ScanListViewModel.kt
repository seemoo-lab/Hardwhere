package de.tu_darmstadt.seemoo.HardWhere.ui.lib

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.data.model.Result
import de.tu_darmstadt.seemoo.HardWhere.data.model.ResultAsset
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.acra.ACRA

/**
 * View Model for unifying scanlist view models containing main list, update, change finish
 * and error integration. To be used with [ScanListFragment]
 */
abstract class ScanListViewModel: ProgressViewModel() {
    /**
     * List of assets to use
     */
    val assetList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    private val _updatedAssets: MutableLiveData<List<Asset>?> = MutableLiveData()
    /**
     * Last time assets got updated
     */
    private var lastUpdate: Long = System.currentTimeMillis()
    /**
     * Set after calling [updatedAssets] when updating got finished.
     * Should be watched by same entity that watches [finishedAssets].
     */
    val updatedAssets: LiveData<List<Asset>?> = _updatedAssets

    /**
     * Reset [updatedAssets], to be called after handling
     */
    fun resetUpdatedAssets() {
        _updatedAssets.value = null
    }

    private val _finishedAssets: MutableLiveData<List<Asset>?> = MutableLiveData()

    /**
     * Assets that finished checkin, to be removed on main thread
     */
    val finishedAssets: LiveData<List<Asset>?> = _finishedAssets

    fun resetFinishedAssets() {
        _finishedAssets.value = null
    }

    /**
     * Filter for items that can be found in the asset list by its tag and set as [_finishedAssets]
     */
    protected fun processFinishedAssets(items: List<Any>, assetList: ArrayList<Asset>) {
        val finished = items.mapNotNull {
            val item = it as Result<ResultAsset>
            assetList.find {
                it.asset_tag == item.payload!!.asset
            }
        }
        _finishedAssets.postValue(finished)
    }

    internal fun updateLastUpdated() {
        lastUpdate = System.currentTimeMillis()
    }
    internal fun lastUpdate(): Long = lastUpdate

    private fun updateAssets(client: APIInterface, assets: Array<Asset>) {
        //TODO: check what happens when an item is gone, we probably fail all items then?
        // pretty bad on auto-fetch after timeout
        // otherwise no callback -> no dec
        if(assets.isNotEmpty())
            incLoading()
        val requests: List<Observable<Asset>> = assets.map {
            client.getAssetObservable(it.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val assets = list.filter {
                if (it is Asset) {
                    return@filter true
                }
                false
            }.map { it as Asset }
            assets
        }
            .subscribe({
                decLoading()
                _updatedAssets.postValue(it)
                updateLastUpdated()
            }) {
                decLoading()
                Log.w(this@ScanListViewModel::class.java.name, "Error: $it")
                _error.postValue(Pair(R.string.error_fetch_update,it))
                ACRA.errorReporter.handleException(it)
            }
    }
}