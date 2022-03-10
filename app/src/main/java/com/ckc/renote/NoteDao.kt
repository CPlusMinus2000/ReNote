package com.ckc.renote

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE name IN (:noteNames)")
    fun loadAllByNames(noteNames: List<String>): List<Note>

    @Query("SELECT * FROM notes WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Note

    @Query("SELECT COUNT(1) FROM notes WHERE name = :name")
    fun noteExists(name: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg notes: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

//@Database(entities = [Note::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun noteDao(): NoteDao
//}