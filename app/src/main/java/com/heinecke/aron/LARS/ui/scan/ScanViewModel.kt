package com.heinecke.aron.LARS.ui.scan

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.Utils.Companion.safeLet
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Asset.Companion.AssetFilter
import com.heinecke.aron.LARS.data.model.SearchResults
import retrofit2.Call

/**
 * ScanViewModel used by [AssetFilterBTFragment], [AssetListFragment], [ScannerFragment] &
 * [AssetSearchFragment]
 */
class ScanViewModel : ViewModel() {
    internal val resolving = MutableLiveData(0)
    internal val scanList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    internal val assetPattern: Regex = Regex("^http.*/([0-9]+)$")
    /**
     * Last time assets got updated
     */
    internal var lastUpdate: Long = System.currentTimeMillis()

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
    internal var lastNetworkCall: Call<SearchResults<Asset>>? = null

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

    internal fun decLoading() {
        resolving.run {
            value = value!! - 1
        }
    }
}