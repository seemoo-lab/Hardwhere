package de.tu_darmstadt.seemoo.LARS.ui.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.JsonElement
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import de.tu_darmstadt.seemoo.LARS.ui.lib.ProgressViewModel
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.Utils.Companion.DEFAULT_LOAD_AMOUNT
import de.tu_darmstadt.seemoo.LARS.data.APIClient
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.SearchResults
import io.sentry.core.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssetListViewModel: ProgressViewModel() {
    internal val _user: MutableLiveData<Selectable.User?> = MutableLiveData(null)
    internal val user: LiveData<Selectable.User?> = _user
    internal val assetList: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    internal val userSearchInput: MutableLiveData<String> = MutableLiveData("")
    private val _userComplectionList: MutableLiveData<List<Selectable.User>> = MutableLiveData(listOf())
    internal val userComplectionList: LiveData<List<Selectable.User>> = _userComplectionList
    private var lastNetworkCall: Call<SearchResults<JsonElement>>? = null

    fun updateUserSearch(data: String?, api: APIInterface) {
        if(userSearchInput.value.equals(data)) {
            return
        }
        userSearchInput.value = data

        incLoading()
        lastNetworkCall?.cancel()

        val call = if (data != null && data.isNotBlank()) {
            api.searchSelectable(Selectable.SelectableType.User.getTypeName(), data)
        } else {
            api.getSelectablePage(Selectable.SelectableType.User.getTypeName(), DEFAULT_LOAD_AMOUNT, 0)
        }

        lastNetworkCall = call
        call.enqueue(object : Callback<SearchResults<JsonElement>?> {
            override fun onResponse(
                call: Call<SearchResults<JsonElement>?>,
                response: Response<SearchResults<JsonElement>?>
            ) {
                decLoading()
                response.run {
                    val elements =
                        this.body()!!.rows.map { elem -> Selectable.SelectableType.User.parseElement(elem) }
                    if (this.isSuccessful) {
                        Log.d(this::class.java.name, "Body: $elements")
                        _userComplectionList.postValue(elements as List<Selectable.User>?)
                    } else {
                        _error.postValue(Pair(R.string.error_fetch_selectable,null))
                    }
                }
            }

            override fun onFailure(call: Call<SearchResults<JsonElement>?>, t: Throwable) {
                decLoading()
                if(!call.isCanceled) {
                    Log.w(this::class.java.name, "$t")
                    _error.postValue(Pair(R.string.error_fetch_selectable,t))
                    t?.apply { Sentry.captureException(this) }
                } else {
                    Log.w(this::class.java.name,"Canceled request")
                }
            }
        })
    }

    fun loadData(client: APIInterface) {
        val user = _user.value ?: return
        incLoading()
        client.getCheckedoutAssets(user.id).enqueue(object : Callback<SearchResults<Asset>> {
            override fun onResponse(call: Call<SearchResults<Asset>>, response: Response<SearchResults<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assets = this.body()!!.rows // TODO: iterate to load all of them if required!
                        assetList.value = assets
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
                Sentry.captureException(t)
            }

        })
    }
}


