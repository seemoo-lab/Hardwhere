package com.heinecke.aron.LARS.data.model

data class Asset(var model: Model?, var category: Category?, var manufacturer: Manufacturer?,
                 var location: Location?, var default_location: Location?, var updated_at: String?, var created_at: String?,
                 var available_actions: Actions,

                 val id: Int = -1,
                 val name: String)