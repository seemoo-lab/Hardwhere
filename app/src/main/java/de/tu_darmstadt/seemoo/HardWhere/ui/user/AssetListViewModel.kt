package de.tu_darmstadt.seemoo.HardWhere.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable
import de.tu_darmstadt.seemoo.HardWhere.ui.lib.ProgressViewModel
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.data.model.SearchResults
import org.acra.ACRA
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssetListViewModel: ProgressViewModel() {
    internal val _user: MutableLiveData<Selectable.User?> = MutableLiveData(null)
    internal val user: LiveData<Selectable.User?> = _user
    internal val assetList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    // Workaround to prevent a race condition on user selection where assetList of user pre-selection is
    // received after the new users assetList, displaying wrong data.
    internal var assetListSource: Selectable.User? = null

    fun loadData(client: APIInterface) {
        val user = _user.value ?: return
        if (user == assetListSource)
            return
        incLoading()
        client.getCheckedoutAssets(user.id).enqueue(object : Callback<SearchResults<Asset>> {
            override fun onResponse(call: Call<SearchResults<Asset>>, response: Response<SearchResults<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assets = this.body()!!.rows // TODO: iterate to load all of them if required!
                        assetList.value = assets
                        assetListSource = user
                        log = false
                    } else {
                        _error.value = Pair(R.string.error_fetch_checkedout,null)
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@AssetListViewModel::class.java,
                        response
                    )
                decLoading()
            }

            override fun onFailure(call: Call<SearchResults<Asset>>, t: Throwable) {
                _error.value = Pair(R.string.error_fetch_checkedout,t)
                decLoading()
                ACRA.errorReporter.handleException(t)
            }

        })
    }
}