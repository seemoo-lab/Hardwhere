package de.tu_darmstadt.seemoo.LARS.ui.lent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.Utils
import de.tu_darmstadt.seemoo.LARS.data.APIInterface
import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LentViewModel : ViewModel() {
    private val _checkedOutAsssets: MutableLiveData<ArrayList<Asset>> = MutableLiveData(ArrayList())
    val checkedOutAsset: LiveData<ArrayList<Asset>> = _checkedOutAsssets
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading
    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error



    fun loadData(client: APIInterface, userID: Int) {
        _loading.value = true
        client.getCheckedOutAssets().enqueue(object : Callback<ArrayList<Asset>> {
            override fun onResponse(call: Call<ArrayList<Asset>>, response: Response<ArrayList<Asset>>) {
                var log = true
                response?.run {
                    if (this.isSuccessful && this.body() != null) {
                        val assetsLent = this.body()!!.filter { a -> a.assigned_to!!.id != userID }
                        _checkedOutAsssets.value!!.clear()
                        _checkedOutAsssets.value!!.addAll(assetsLent)
                        log = false
                    } else {
                        _error.value = "Failed to load checked out assets!"
                    }
                }
                if(log)
                    Utils.logResponseVerbose(
                        this@LentViewModel::class.java,
                        response
                    )
                _loading.value = false
            }

            override fun onFailure(call: Call<ArrayList<Asset>>, t: Throwable) {
                _error.value = "Failed to load checked out assets!"
                _loading.value = false
            }

        })
    }


}