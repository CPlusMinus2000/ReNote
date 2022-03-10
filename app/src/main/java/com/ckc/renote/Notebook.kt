package com.ckc.renote
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "order") val order: Int
)