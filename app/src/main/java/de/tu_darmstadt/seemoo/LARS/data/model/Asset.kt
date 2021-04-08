package de.tu_darmstadt.seemoo.LARS.data.model

import android.os.Parcelable
import com.google.gson.JsonObject
import de.tu_darmstadt.seemoo.LARS.R
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Asset(
    var model: Model?, var category: Category?,
    /**
     * <b>Non editable<b>, inherited from model
     */
    val manufacturer: Manufacturer?,
    var rtd_location: Location?, var updated_at: Date?,
    var date_at: Date?, var available_actions: Actions?,
    /** note that an invalid deserialization results in id = 0 **/
    val id: Int = 0,
    var name: String?, var notes: String?,
    var asset_tag: String?,
    @Transient
    var selected: Boolean = false,
    val assigned_to: AssignUser?,
    /** Custom fields **/
    var custom_fields: HashMap<String,CustomField>?
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Asset

        return id == other.id
    }

    companion object {
        /**
         * Create a new empty asset
         * @param multiAsset Set true if multi-asset type is wanted, otherwise newAsset is set
         */
        fun getEmptyAsset(multiAsset: Boolean): Asset {
            return Asset(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                if (multiAsset) ID_MULTI_ASSET else ID_NEW_ASSET,
                null,
                null,
                // has to be null, to allow patch-asset creation for multi-update, where the tag isn't set
                null,
                false,
                null,
                null,
            )
        }

        @JvmField
        val ID_MULTI_ASSET = -1
        @JvmField
        val ID_NEW_ASSET = 0
        @JvmField
        val FIELD_NOTES = "notes"
        @JvmField
        val FIELD_TAG = "asset_tag"
        @JvmField
        val FIELD_NAME = "name"
        @JvmField
        val FIELD_MODEL_ID = "model_id"
        @JvmField
        val FIELD_RTD_LOCATION_ID = "rtd_location_id"
        @JvmField
        val FIELD_CHECKOUT_ASSET = "asset"
        @JvmField
        val FIELD_CHECKOUT_USER = "user"


        /**
         * Helper enum for asset search filtering by field.
         *
         * It makes the bigger amount of the dirty work required when still using hard typing and no reflections
         */
        enum class AssetFilter(val value: Int) {
            None(R.string.no_filter) {
                override fun contains(asset: Asset, input: String): Boolean = true
            },
            Model(R.string.hint_model) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.model,input)
            },
            Category(R.string.hint_category) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.category,input)
            },
            Location(R.string.hint_default_location) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.rtd_location,input)
            },
            Name(R.string.hint_asset_name) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.name,input)
            },
            Notes(R.string.hint_note) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.notes,input)
            },
            Tag(R.string.hint_asset_tag) {
                override fun contains(asset: Asset, input: String): Boolean = contains(asset.asset_tag,input)
            };

            /**
             * Returns whether the asset matches this filter
             */
            abstract fun contains(asset: Asset, input: String): Boolean

            protected fun <T: Selectable> contains(sel: T?, input: String): Boolean {
                return sel?.name?.contains(input, ignoreCase = true) ?: false
            }
            protected fun contains(sel: String?, input: String): Boolean {
                return sel?.contains(input, ignoreCase = true) ?: false
            }
        }
    }

    /**
     * Create an asset patch for updating assets. The resulting JsonObject only contains
     * the fields that are non-null.
     */
    fun createPatch(custom_fields: Boolean): JsonObject {
        val multiEdit = this.isMultiAsset()
        val base = JsonObject()
        this.model?.run { base.addProperty(FIELD_MODEL_ID, this.id) }
        this.rtd_location?.run { base.addProperty(FIELD_RTD_LOCATION_ID, this.id) }
        @Suppress("UNNECESSARY_SAFE_CALL")
        this.name?.run { if(!this.isBlank() || !multiEdit) base.addProperty(FIELD_NAME, this) }
        @Suppress("UNNECESSARY_SAFE_CALL")
        this.notes?.run { if(!this.isBlank() || !multiEdit) base.addProperty(FIELD_NOTES, this) }
        if(!multiEdit) {
            this.asset_tag?.run { base.addProperty(FIELD_TAG, this) }
        }
        if(custom_fields) {
            this.custom_fields?.run {
                for (attrib in this) {
                    if (attrib.value.value != null)
                        base.addProperty(attrib.value.field,attrib.value.value)
                }
            }
        }

        return base
    }

    /**
     * Create asset checkout JsonObject with checkout to specified user.
     */
    fun createCheckout(user: Int): JsonObject {
        val base = JsonObject()
        base.addProperty(FIELD_CHECKOUT_ASSET,this.id)
        base.addProperty(FIELD_CHECKOUT_USER, user)
        return base
    }

    /**
     * Create asset checkin JsonObject.
     */
    fun createCheckin(): JsonObject {
        val base = JsonObject()
        base.addProperty(FIELD_CHECKOUT_ASSET,this.id)
        return base
    }

    fun isMultiAsset(): Boolean {
        return id == ID_MULTI_ASSET
    }

    fun isNewAsset(): Boolean {
        return id == ID_NEW_ASSET
    }
}