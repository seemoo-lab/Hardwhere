package com.heinecke.aron.LARS.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable
import com.heinecke.aron.LARS.ui.editor.SelectorFragment.SelectorData

class EditorViewModel : ViewModel() {
    private val assetMutable: MutableLiveData<Asset> = MutableLiveData()
    internal var asset: LiveData<Asset> = assetMutable

    fun EditorViewModel() {
        // trigger user load.
    }

    fun setEditorAsset(asset: Asset) {
        assetMutable.value = asset
    }
}