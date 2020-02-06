package com.heinecke.aron.LARS.ui.scan

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.heinecke.aron.LARS.Utils.Companion.safeLet
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Asset.Companion.AssetFilter
import com.heinecke.aron.LARS.data.model.SearchResults
import com.heinecke.aron.LARS.ui.editor.SelectorFragment
import retrofit2.Call

/**
 * ScanViewModel used by [AssetFilterBTFragment], [AssetListFragment], [ScannerFragment] &
 * [AssetSearchFragment]
 */
class ScanViewModel : ViewModel() {
    internal val resolving = MutableLiveData(0)
    internal val scanList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    internal val assetPattern: Regex = Regex("^http.*/([0-9]+)$")
    @JvmField val S_SCAN_LIST: String = "scan_list"
    @JvmField val S_UPDATE_TIME: String = "update_time"
    /**
     * Last time assets got updated
     */
    private var lastUpdate: Long = System.currentTimeMillis()

    // Search Fragment

    /**
     * Search input string
     */
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    /**
     * Raw results from server
     */
    internal val searchFetchData: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())

    // Filter
    internal val filterMode: MutableLiveData<AssetFilter> = MutableLiveData(AssetFilter.None)
    /**
     * Filtered data for view
     */
    internal val searchFiltered: MediatorLiveData<List<Asset>> = MediatorLiveData()

    init {
        searchFiltered.addSource(searchFetchData) {
            searchFiltered.value = updateFilteredAssets()
        }
        searchFiltered.addSource(filterMode){
            searchFiltered.value = updateFilteredAssets()
        }
    }
    private var lastNetworkCall: Call<SearchResults<Asset>>? = null

    fun cancelNetworkCall() {
        lastNetworkCall?.run {
            cancel()
        }
    }

    fun setNetworkCall(call: Call<SearchResults<Asset>>) {
        cancelNetworkCall()
        lastNetworkCall = call
    }

    internal fun incLoading() {
        resolving.run {
            value = value!! + 1
        }
    }

    private fun updateFilteredAssets(): List<Asset>
    {
        return safeLet(filterMode.value, searchString.value, searchFetchData.value) { filter, search, data ->
            data.filter { x -> filter.contains(x, search) }
        } ?: searchFiltered.value ?: listOf()
    }

    internal fun saveViewModelState(outState: Bundle) {
        outState.putParcelableArrayList(S_SCAN_LIST, scanList.value)
        outState.putLong(S_UPDATE_TIME,lastUpdate())
    }
    internal fun restoreViewModelState(state: Bundle) {
        val scanList = scanList.value!!
        // don't re-add the same list again on rotation, only on kill restore
        if(scanList.size == 0) {
            scanList.addAll(state.getParcelableArrayList(S_SCAN_LIST)!!)
            lastUpdate = state.getLong(S_UPDATE_TIME)
        }
    }
    internal fun updateLastUpdated() {
        lastUpdate = System.currentTimeMillis()
    }
    internal fun lastUpdate(): Long = lastUpdate

    internal fun decLoading() {
        resolving.run {
            value = if(value!! > 0)
                value!! - 1
            else
                0
            Log.d(this@ScanViewModel::class.java.name,"Loading: $value")
        }
    }
}