package com.heinecke.aron.LARS.data.model

data class Asset(var model: Model?, var category: Category?, var manufacturer: Manufacturer?,
                 var rtd_location: Location?, var updated_at: Date?,
                 var date_at: Date?, var available_actions: Actions?,
                 /** note that an invalid deserialization results in id = 0 **/
                 val id: Int = 0,
                 val name: String, val notes: String)