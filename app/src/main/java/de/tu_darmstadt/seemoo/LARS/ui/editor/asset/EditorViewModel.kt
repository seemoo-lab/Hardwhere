package de.tu_darmstadt.seemoo.LARS.ui.editor.asset

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.CustomField
import de.tu_darmstadt.seemoo.LARS.data.model.Result
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.function.Predicate


class EditorViewModel : ViewModel() {
    /**
     * Asset used to display and edit-base
     */
    internal val assetMutable: MutableLiveData<Asset> = MutableLiveData()
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

    val customtomAttributeFields: LiveData<HashSet<String>> = Transformations.map(multiEditAssets) { assets ->
        customAttributes(assets)
    }

    /**
     * Sets the asset to use in editor and as original value
     */
    fun setEditorAsset(asset: Asset) {
        Log.d(this::class.java.name,"Setting editor asset & default")
        assetMutable.value = asset
        assetOriginMutable.value = assetMutable.value!!.copy()
    }

    /**
     * Check if only a single model is currently to be edited, used for custom fields
     */
    fun isSingleModel(): Boolean {
        multiEditAssets.value?.run {
            if (this.size == 1) {
                return true
            }
            var model = this[0].model

            for (elem in this) {
                if (elem.model != model)
                    return false
            }
            return true
        }
        return true
    }

    /**
     * Returns a list of custom attributes that all match
     */
    private fun customAttributes(assets: ArrayList<Asset>): HashSet<String> {
        if (assets.size > 0) {
            var keys: HashSet<String>? = null
            // TODO: check if non-set custom attribs are always set of have to be retrieved by model
            for (asset in assets) {
                if (keys == null)
                    keys = asset.custom_fields?.keys?.toHashSet()
                else
                    asset.custom_fields?.run { keys.removeIf{t -> !this.containsKey(t)} }

            }
            return keys?: HashSet()
        }
        return HashSet()
    }

    fun updateAssets(client: APIInterface) {
        loading.value =
            Loading()

        val requests: MutableList<Observable<Result<Asset>>> = mutableListOf()
        val singleAsset = asset.value!!
        val patchData = singleAsset.createPatch(isSingleModel())
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