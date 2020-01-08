package com.heinecke.aron.LARS

import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.data.model.Selectable
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
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
        // name should not get null, only ""
        assertEquals("",dpAsset2.name)
        assertEquals(null,dpAsset2.model)
    }


}
