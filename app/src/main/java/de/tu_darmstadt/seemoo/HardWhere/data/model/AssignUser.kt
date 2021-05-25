package de.tu_darmstadt.seemoo.HardWhere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignUser(
    val id: Int,
    val name: String,
    val first_name: String,
    val last_name: String,
    val username: String,
) : Parcelable {
    fun asSelector(): Selectable.User = Selectable.User(id,name,"<todo>")
}
