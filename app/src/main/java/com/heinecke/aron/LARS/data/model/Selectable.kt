package com.heinecke.aron.LARS.data.model

import android.os.Parcelable

/**
 * Interface for models that have a name & id, used for selection dialogs
 */
interface Selectable : Parcelable {
    val name: String
    val id: Int
}