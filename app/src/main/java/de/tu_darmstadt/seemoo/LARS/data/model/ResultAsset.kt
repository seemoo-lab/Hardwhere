package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Returned as payload for snipeit api results from checkin/checkout
 */
@Parcelize
data class ResultAsset (
    /**
     * Tag of asset
     */
    val asset: String
) : Parcelable