package com.ckc.renote
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    var contents: String,
    var name: String,
    val creationTime: Long,
    val lastEdited: Long,
    val customOrder: Int?
)
