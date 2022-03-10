package com.ckc.renote
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "contents") var contents: String,
    @PrimaryKey var name: String,
    @ColumnInfo(name = "creation_time") val creationTime: Long,
    @ColumnInfo(name = "last_edited") var lastEdited: Long,
    @ColumnInfo(name = "custom_order") val customOrder: Int,
    @ColumnInfo(name = "notebook_name") val notebookName: String
)
