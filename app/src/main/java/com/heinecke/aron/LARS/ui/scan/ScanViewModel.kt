package com.heinecke.aron.LARS.ui.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.SearchResults
import retrofit2.Call

class ScanViewModel : ViewModel() {
    internal val resolving = MutableLiveData(0)
    internal val scanList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    internal val assetPattern: Regex = Regex("^http.*/([0-9]+)$")

    // Search Fragment
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    internal val searchResults: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    internal var lastNetworkCall: Call<SearchResults<Asset>>? = null
    // Filter
    internal val filterMode: MutableLiveData<Asset.Companion.AssetFilter> = MutableLiveData()

    internal fun incLoading() {
        resolving.run {
            value = value!! + 1
        }
    }

    internal fun decLoading() {
        resolving.run {
            value = value!! - 1
        }
    }
}