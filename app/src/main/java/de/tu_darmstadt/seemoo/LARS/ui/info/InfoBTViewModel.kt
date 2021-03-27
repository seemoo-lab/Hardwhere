package de.tu_darmstadt.seemoo.LARS.ui.info

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tu_darmstadt.seemoo.LARS.data.model.Asset

class InfoBTViewModel : ViewModel() {

    @JvmField val S_INFO_ASSET: String = "info_asset"
    /**
     * Asset to show AssetInfoBTFragment for
     */
    internal val infoAsset: MutableLiveData<Asset?> = MutableLiveData()

    internal fun saveViewModelState(outState: Bundle) {
        outState.putParcelable(S_INFO_ASSET,infoAsset.value)
    }
    internal fun restoreViewModelState(state: Bundle) {
        if (infoAsset.value == null)
            infoAsset.value = state.getParcelable(S_INFO_ASSET)
    }
}