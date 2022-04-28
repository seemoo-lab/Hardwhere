package de.tu_darmstadt.seemoo.HardWhere.ui.myassets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.data.model.SearchResults
import org.acra.ACRA
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAssetsViewModel : ViewModel() {
    private val _checkedOutAssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())

    internal val filterMode: MutableLiveData<Asset.Companion.AssetExactFilter> = MutableLiveData(Asset.Companion.AssetExactFilter.None)
    internal val sortMode: MutableLiveData<Asset.Companion.AssetSorter> = MutableLiveData(Asset.Companion.AssetSorter.None)

    val checkedOutAsset: LiveData<List<Asset>> = MediatorLiveData<List<Asset>>().apply {
        fun update() {
            val filter = filterMode.value!!
            val list = _checkedOutAssets.value
            val sorter = sortMode.value!!
            if (list == null) {
                value = listOf()
                return
            }
            var processedList: List<Asset> = if (filter == Asset.Companion.AssetExactFilter.None) {
                list
            } else {
                list.filter { asset ->
                    filter.isExact(asset)
                }
            }

            if (sorter != Asset.Companion.AssetSorter.None) {
                value = processedList.sortedBy {
                    sorter.sortKey(it)
                }
            } else {
                value = processedList
            }
        }

        addSource(sortMode) {update()}
        addSource(filterMode) {update()}
        addSource(_checkedOutAssets) { update()}
        update()
    }
    private val _loading: MutableLiveData<Boolean> = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error


    fun resetError() {
        _error.value = null
    }

    fun loadData(client: APIInterface, userID: Int) {
        _loading.value = true
        client.getCheckedoutAssets(userID).enqueue(object : Callback<SearchResults<Asset>> {
            override fun onResponse(call: Call<SearchResults<Asset>>, response: Response<SearchResults<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assets = this.body()!!.rows // TODO: iterate to load all of them if required!
                        _checkedOutAssets.value = assets
                        log = false
                    } else {
                        _error.value = "Failed to load checked out assets!"
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@MyAssetsViewModel::class.java,
                        response
                    )
                _loading.value = false
            }

            override fun onFailure(call: Call<SearchResults<Asset>>, t: Throwable) {
                _error.value = "Failed to load checked out assets!"
                _loading.value = false
                ACRA.errorReporter.handleException(t)
            }

        })
    }
}