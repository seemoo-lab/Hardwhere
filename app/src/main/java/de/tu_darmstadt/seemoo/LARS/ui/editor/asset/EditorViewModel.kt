package de.tu_darmstadt.seemoo.LARS.ui.editor.asset

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Result
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers


class EditorViewModel : ViewModel() {
    /**
     * Asset used to display and edit-base
     */
    private val assetMutable: MutableLiveData<Asset> = MutableLiveData()
    internal var asset: LiveData<Asset> = assetMutable
    /**
     * Asset containing the original values. Allows detecting the original value on rotation and
     * reset functionality.
     */
    private var assetOriginMutable: MutableLiveData<Asset> = MutableLiveData()
    internal var assetOrigin: LiveData<Asset> = assetOriginMutable
    /**
     * Assets passed on multi-edit to update
     */
    val multiEditAssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData()
    val loading: MutableLiveData<Loading?> = MutableLiveData(null)
    /**
     *  Non-Null signals editing finished to other fragments
     *  Observators are required to call reset() on non-null value
     */
    val editingFinished: MutableLiveData<Any?> = MutableLiveData(null)

    /**
     * Sets the asset to use in editor and as original value
     */
    fun setEditorAsset(asset: Asset) {
        Log.d(this::class.java.name,"Setting editor asset & default")
        assetMutable.value = asset
        assetOriginMutable.value = assetMutable.value!!.copy()
    }

    fun updateAssets(client: APIInterface) {
        loading.value =
            Loading()

        val requests: MutableList<Observable<Result<Asset>>> = mutableListOf()
        val singleAsset = asset.value!!
        val patchData = singleAsset.createPatch()
        if (!singleAsset.isMultiAsset()) {
            requests.add(
                client.updateAsset(singleAsset.id, patchData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
            )
        } else {
            requests.addAll(multiEditAssets.value!!.map {
                client.updateAsset(it.id, patchData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
            })
        }

        @Suppress("UNUSED_VARIABLE")
        val ignored = Observable.zip(requests) { list ->
            val failed = list.filter {
                if (it is Result<*>) {
                    if (it.status == "success") {
                        return@filter false
                    }
                }
                true
            }
            failed
        }
            .subscribe({
                Log.d(this@EditorViewModel::class.java.name, "Finished with $it")
                loading.postValue(
                    Loading(
                        null,
                        it.isEmpty()
                    )
                )
                editingFinished.postValue(1)
            }) {
                Log.w(this@EditorViewModel::class.java.name, "Error: $it")
                loading.postValue(
                    Loading(
                        it,
                        false
                    )
                )
            }
    }

    fun reset() {
        editingFinished.value = null
    }

    data class Loading(
        val error: Throwable? = null,
        /**
         * non-null on finish
         */
        val success: Boolean? = null
    )
}