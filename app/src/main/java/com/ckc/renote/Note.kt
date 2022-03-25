package com.ckc.renote
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "contents") var contents: String,
    @PrimaryKey var name: String,
    @ColumnInfo(name = "creation_time") val creationTime: Long,
    @ColumnInfo(name = "last_edited") var lastEdited: Long,
    @ColumnInfo(name = "custom_order") val customOrder: Int,
    @ColumnInfo(name = "notebook_name") val notebookName: String,
    @ColumnInfo(name = "recording", typeAffinity = ColumnInfo.TEXT) var recording: Recording? = null,
    @ColumnInfo(name = "server_id") val serverId: String? = null
)

class Converters {
    @TypeConverter
    fun fromRecording(recording: Recording?): String? {
        return if (recording == null) {
            null
        } else {
            Json.encodeToString(recording)
        }
    }

    @TypeConverter
    fun toRecording(recording: String?): Recording? {
        return if (recording == null) {
            null
        } else {
            Json.decodeFromString<Recording>(recording)
        }
    }
}
