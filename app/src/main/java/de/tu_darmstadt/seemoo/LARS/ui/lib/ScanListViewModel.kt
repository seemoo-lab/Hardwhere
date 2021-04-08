package de.tu_darmstadt.seemoo.LARS.ui.lib

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Result
import de.tu_darmstadt.seemoo.LARS.data.model.ResultAsset

abstract class ScanListViewModel: ProgressViewModel() {
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
}