package com.ckc.renote
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey var name: String,
    @ColumnInfo(name = "notebook_order") val order: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_modified") val lastModified: Long
)