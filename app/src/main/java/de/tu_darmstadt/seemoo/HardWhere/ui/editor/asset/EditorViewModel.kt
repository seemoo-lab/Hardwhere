package de.tu_darmstadt.seemoo.HardWhere.ui.editor.asset

import android.telecom.Call
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.HardWhere.Utils.Companion.logResponseVerbose
import de.tu_darmstadt.seemoo.HardWhere.data.APIInterface
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.data.model.FieldSet
import de.tu_darmstadt.seemoo.HardWhere.data.model.Model
import de.tu_darmstadt.seemoo.HardWhere.data.model.Result
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.acra.ACRA
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Field


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

    /**
     * Loading indicator for editor
     */
    val loading: MutableLiveData<Loading?> = MutableLiveData(null)

    /**
     * Error channel _not_ used for editing calls, instead for loading
     */
    val errorChannel: MutableLiveData<Loading?> = MutableLiveData(null)
    /**
     *  Non-Null signals editing finished to other fragments
     *  Observators are required to call reset() on non-null value
     */
    val editingFinished: MutableLiveData<Any?> = MutableLiveData(null)

    class CustomAttributeData(val state: CustomAttributeState, val error: Throwable? = null, val data: FieldSet? = null)
    enum class CustomAttributeState {
        LOADING,

        /**
         * Multiple assets have different models
         */
        MISMATCH,
        LOADED,
        FAILED,
        UNINITIALIZED,

        /**
         * No model
         */
        NONE,
    }

    val _fieldset: MutableLiveData<CustomAttributeData> = MutableLiveData(CustomAttributeData(CustomAttributeState.UNINITIALIZED))
    val fieldSet: LiveData<CustomAttributeData> = _fieldset

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
     * Fetch custom fieldset for assets. Abort if not all assets have the same model.
     */
    fun fetchModel(client: APIInterface) {
        Log.d(this::class.java.name,"fetchModel")
        val assets: ArrayList<Asset> = multiEditAssets.value!!
        _fieldset.value = CustomAttributeData(CustomAttributeState.LOADING)

        val requestAsset = if(assets.size > 0) {
            assets[0]
        } else {
            asset.value
        }
        if (requestAsset?.model == null) {
            Log.d(this::class.java.name,"no asset with model found, aborting model fetching")
            return
        }
        Log.d(this::class.java.name,"asset: $requestAsset")

        if (assets.size > 0) {
            if (assets.find { a -> a.model!!.id != assets[0].model!!.id } != null) {
                _fieldset.value = CustomAttributeData(CustomAttributeState.MISMATCH)
                return
            }
        }
        // retrieve model
        client.getModel(requestAsset.model!!.id).enqueue(object: Callback<Model> {
            override fun onResponse(call: retrofit2.Call<Model>, response: Response<Model>) {
                response.body()?.run {
                    if (this.fieldset != null) {
                        // retrieve fieldset of model
                        client.getFieldset(this.fieldset.id).enqueue(object: Callback<FieldSet>{
                            override fun onResponse(
                                call: retrofit2.Call<FieldSet>,
                                response: Response<FieldSet>
                            ) {
                                response.body()?.run {
                                    _fieldset.value = CustomAttributeData(CustomAttributeState.LOADED, null,this)
                                } ?: logResponseVerbose(this@EditorViewModel::class.java, response).also {
                                    _fieldset.value = CustomAttributeData(CustomAttributeState.FAILED, RuntimeException(response.message()))
                                    ACRA.errorReporter.handleException(RuntimeException(response.message()))
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<FieldSet>, t: Throwable) {
                                Log.wtf(this@EditorViewModel::class.java.name, "Failed to fetch fieldset",t)
                                _fieldset.value = CustomAttributeData(CustomAttributeState.FAILED, RuntimeException(response.message()))
                                ACRA.errorReporter.handleException(RuntimeException(response.message()))
                            }
                        })
                    } else {
                        Log.d(this@EditorViewModel::class.java.name, "No fieldset for this model")
                        _fieldset.value = CustomAttributeData(CustomAttributeState.NONE)
                    }
                } ?: logResponseVerbose(this@EditorViewModel::class.java, response).also {
                    _fieldset.value = CustomAttributeData(CustomAttributeState.FAILED, RuntimeException(response.message()))
                    ACRA.errorReporter.handleException(RuntimeException(response.message()))
                }
            }

            override fun onFailure(call: retrofit2.Call<Model>, t: Throwable) {
                Log.wtf(this@EditorViewModel::class.java.name, "Failed to fetch model",t)
                loading.postValue(
                    Loading(
                        t,
                        false
                    )
                )
                _fieldset.value = CustomAttributeData(CustomAttributeState.FAILED, t)
                ACRA.errorReporter.handleException(t)
            }

        })
    }

    fun updateAssets(client: APIInterface) {
        if (multiEditAssets.value!!.isNotEmpty())
            loading.value = Loading()

        val requests: MutableList<Observable<Result<Void>>> = mutableListOf()
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
                ACRA.errorReporter.handleException(it)
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