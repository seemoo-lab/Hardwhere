package de.tu_darmstadt.seemoo.LARS

import de.tu_darmstadt.seemoo.LARS.data.model.Asset
import de.tu_darmstadt.seemoo.LARS.data.model.Asset.Companion.FIELD_NAME
import de.tu_darmstadt.seemoo.LARS.data.model.Asset.Companion.FIELD_NOTES
import de.tu_darmstadt.seemoo.LARS.data.model.Asset.Companion.FIELD_TAG
import de.tu_darmstadt.seemoo.LARS.data.model.Selectable
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AssetUnitTest {
    @Test
    fun check_reference_rules() {
        val value = Asset.getEmptyAsset(true)
        value.name = "test"

        var name = value.name
        name = null
        assertEquals("test",value.name)
    }

    @Test
    fun getEqualAssetAttributes() {

        val asset1 = Asset.getEmptyAsset(false)
        asset1.run {
            model = Selectable.Model(1, "asd1")
            rtd_location = Selectable.Location(2, "asd")
            name = "test1"
        }
        val asset2 = Asset.getEmptyAsset(false)
        asset2.run {
            model = Selectable.Model(1,"asd2")
            rtd_location = Selectable.Location(2, "asd")
            name = "test1"
        }

        val dpAsset1 = Asset.getEmptyAsset(true)
        val assets = arrayListOf(asset1,asset2)
        Utils.getEqualAssetAttributes(dpAsset1,assets)
        // same name
        assertEquals("test1",dpAsset1.name)
        assertEquals(Selectable.Location(2,"asd"),dpAsset1.rtd_location)
        // verify only ID is checked
        assertEquals(Selectable.Model(1,"asd1"),dpAsset1.model)


        val dpAsset2 = Asset.getEmptyAsset(true)
        // check for no crash on null-asset
        Utils.getEqualAssetAttributes(dpAsset2, arrayListOf(asset1,Asset.getEmptyAsset(true),asset2))
        assertEquals(null,dpAsset2.name)
        assertEquals(null,dpAsset2.model)
    }

    /**
     * Verify that empty fields do not end up in the patch
     */
    @Test
    fun assetPatchEmptyString() {
        val asset = Asset.getEmptyAsset(true)
        asset.name = ""
        asset.notes = ""
        asset.asset_tag = ""
        val patch = asset.createPatch()
        assertFalse(patch.has(FIELD_NAME))
        assertFalse(patch.has(FIELD_NOTES))
        assertFalse(patch.has(FIELD_TAG))
    }

    /**
     * Sanity check that non empty fields are included, excluding tag on multiAsset
     */
    @Test
    fun assetPatchNonEmpty() {
        val asset = Asset.getEmptyAsset(true)
        asset.name = "a"
        asset.notes = "b"
        asset.asset_tag = "c"
        val patch = asset.createPatch()
        assertEquals("a", patch.get(FIELD_NAME)!!.asString)
        assertEquals("b", patch.get(FIELD_NOTES)!!.asString)
        assertFalse(patch.has(FIELD_TAG))
    }

    /**
     * Verify all fields included on non multi-edit
     */
    @Test
    fun assetPatchNonMultiEdit() {
        val asset = Asset.getEmptyAsset(false)
        asset.name = "a"
        asset.notes = "b"
        asset.asset_tag = "c"
        val patch = asset.createPatch()
        assertEquals("a", patch.get(FIELD_NAME)!!.asString)
        assertEquals("b", patch.get(FIELD_NOTES)!!.asString)
        assertEquals("c", patch.get(FIELD_TAG)!!.asString)
    }

    /**
     * Verify all fields included on non multi-edit
     */
    @Test
    fun assetPatchNonMultiEditEmpty() {
        val asset = Asset.getEmptyAsset(false)
        asset.name = ""
        asset.notes = ""
        asset.asset_tag = ""
        val patch = asset.createPatch()
        assertEquals("", patch.get(FIELD_NAME)!!.asString)
        assertEquals("", patch.get(FIELD_NOTES)!!.asString)
        assertEquals("", patch.get(FIELD_TAG)!!.asString) // "" shouldn't happen as input, still valid
    }
}
