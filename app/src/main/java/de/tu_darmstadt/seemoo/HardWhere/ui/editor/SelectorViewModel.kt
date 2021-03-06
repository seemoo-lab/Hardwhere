package de.tu_darmstadt.seemoo.HardWhere.ui.editor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import de.tu_darmstadt.seemoo.HardWhere.data.model.SearchResults
import de.tu_darmstadt.seemoo.HardWhere.data.model.Selectable
import retrofit2.Call

class SelectorViewModel : ViewModel() {
    internal val resolving = MutableLiveData(0)
    internal val data: MutableLiveData<ArrayList<Selectable>> = MutableLiveData(ArrayList())
    internal val searchString: MutableLiveData<String> = MutableLiveData("")
    private var lastNetworkCall: Call<SearchResults<JsonElement>>? = null
    /**
     * Storage for last type to allow cross-select persistence
     */
    internal val lastType: MutableLiveData<Selectable.SelectableType?> = MutableLiveData(null)
    /**
     * Selected item. Observe this to obtain selection of dialog. Call resetSelected once a value got selected
     */
    val selected: MutableLiveData<SelectorFragment.SelectorData?> =
        MutableLiveData()

    fun cancelNetworkCall() {
        lastNetworkCall?.run {
            cancel()
        }
    }

    fun setNetworkCall(call: Call<SearchResults<JsonElement>>) {
        cancelNetworkCall()
        lastNetworkCall = call
    }

    fun setSelected(item: SelectorFragment.SelectorData) {
        selected.value = item
    }

    /**
     * Reset item in selected
     */
    fun resetSelected() {
        selected.value = null
        searchString.value = ""
    }

    /**
     * Reset search string, internal use
     */
    fun resetSearchString() {
        Log.d(this::class.java.name, "ResetSearchString")
        searchString.value = ""
    }


    internal fun incLoading() {
        resolving.run {
            value = value!! + 1
        }
    }

    internal fun decLoading() {
        resolving.run {
            value = if(value!! > 0)
                value!! - 1
            else
                0
        }
    }
}