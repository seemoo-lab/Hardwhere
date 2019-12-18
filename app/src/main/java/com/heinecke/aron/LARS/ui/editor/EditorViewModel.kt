package com.heinecke.aron.LARS.ui.editor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.APIInterface
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Result
import io.reactivex.Observable
import io.reactivex.functions.Function


class EditorViewModel : ViewModel() {
    private val assetMutable: MutableLiveData<Asset> = MutableLiveData()
    internal var asset: LiveData<Asset> = assetMutable
    val multiEditAssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData()
    val loading: MutableLiveData<Loading?> = MutableLiveData(null)

    fun EditorViewModel() {
        // trigger user load.
    }

    fun setEditorAsset(asset: Asset) {
        assetMutable.value = asset
    }

    fun updateAssets(client: APIInterface) {
        loading.value = Loading()

        val requests: MutableList<Observable<Result>> = mutableListOf()
        val singleAsset = asset.value!!
        val patchData = APIInterface.makeAssetPatch(singleAsset)
        if (!singleAsset.isMultiAsset()) {
            requests.add(client.updateAsset(singleAsset.id,patchData))
        } else {
            requests.addAll(multiEditAssets.value!!.map {
                client.updateAsset(it.id,patchData)
            })
        }

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val failed = list.filter {
                if (it is Result) {
                    (it as Result).status.equals("success")
                } else {
                    false
                }
            }
            failed
        }
            .subscribe({
                Log.d(this@EditorViewModel::class.java.name,"Finished with $it")
                loading.value = Loading(null,it.isEmpty())
            }) {
                Log.w(this@EditorViewModel::class.java.name,"Error: $it")
                loading.value = Loading(it)
            }
    }

    data class Loading(val error: Throwable? = null, val success: Boolean? = null)
}