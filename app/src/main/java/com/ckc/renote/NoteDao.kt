package com.ckc.renote

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE name IN (:noteNames)")
    fun loadAllByNames(noteNames: List<String>): List<Note>

    @Query("SELECT * FROM notebooks ORDER BY notebook_order ASC")
    fun loadNotebooksInOrder(): List<Notebook>

    @Query("SELECT * FROM notebooks ORDER BY created_at ASC")
    fun loadNotebooksCreatedAt(): List<Notebook>

    @Query("SELECT * FROM notebooks ORDER BY last_modified DESC")
    fun loadNotebooksLastModified(): List<Notebook>

    @Query("SELECT MAX(custom_order) FROM notes")
    fun getMaxCustomOrder(): Int

    @Query("SELECT MAX(notebook_order) FROM notebooks")
    fun getMaxNotebookOrder(): Int

    @Query("SELECT * FROM notes WHERE notebook_name = :notebookName ORDER BY custom_order ASC")
    fun loadNotesInOrder(notebookName: String): List<Note>

    // Get the notes by order of creation time
    @Query("SELECT * FROM notes WHERE notebook_name = :notebookName ORDER BY creation_time ASC")
    fun loadNotesByCreatedAt(notebookName: String): List<Note>

    // Get the notes by order of last modified time
    @Query("SELECT * FROM notes WHERE notebook_name = :notebookName ORDER BY last_edited DESC")
    fun loadNotesByLastModified(notebookName: String): List<Note>

    @Query("SELECT * FROM notes WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Note

    @Query("SELECT * FROM notebooks WHERE name LIKE :name LIMIT 1")
    fun findNotebookByName(name: String): Notebook

    @Query("SELECT COUNT(1) FROM notes WHERE name = :name")
    fun noteExists(name: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg notes: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotebook(notebook: Notebook)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note)

    @Query("UPDATE notes SET last_edited = :lastEdited WHERE name = :name")
    fun update(name: String, lastEdited: Long)

    @Query("UPDATE notebooks SET last_modified = :lastEdited WHERE name = :name")
    fun updateNotebook(name: String, lastEdited: Long)

    @Query("SELECT * FROM notes WHERE notebook_name = :notebookName AND custom_order < :customOrder ORDER BY custom_order DESC LIMIT 1")
    fun loadPreviousNoteInOrder(notebookName: String, customOrder: Int): Note?

    @Query("SELECT * FROM notes WHERE notebook_name = :notebookName AND custom_order > :customOrder ORDER BY custom_order ASC LIMIT 1")
    fun loadNextNoteInOrder(notebookName: String, customOrder: Int): Note?

    @Query("SELECT * FROM notebooks WHERE notebook_order < :order ORDER BY notebook_order DESC LIMIT 1")
    fun loadPreviousNotebookInOrder(order: Int): Notebook?

    @Query("SELECT * FROM notebooks WHERE notebook_order > :order ORDER BY notebook_order ASC LIMIT 1")
    fun loadNextNotebookInOrder(order: Int): Notebook?

    // Count number of notebooks
    @Query("SELECT COUNT(1) FROM notebooks")
    fun notebookCount(): Int

    // Count total number of notes
    @Query("SELECT COUNT(1) FROM notes")
    fun noteCount(): Int

    // Count number of notes in a notebook
    @Query("SELECT COUNT(1) FROM notes WHERE notebook_name = :notebookName")
    fun noteCountInNotebook(notebookName: String): Int

    // Select the most recently modified note
    @Query("SELECT name FROM notes ORDER BY last_edited DESC LIMIT 1")
    fun getMostRecentlyModifiedNote(): String?

    // Get the notebooks sorted by creation time
    @Query("SELECT * FROM notebooks ORDER BY created_at ASC")
    fun getNotebooksSortedByCreationTime(): List<Notebook>

    // Get the notebooks sorted by modification time
    @Query("SELECT * FROM notebooks ORDER BY last_modified DESC")
    fun getNotebooksSortedByModificationTime(): List<Notebook>

    @Delete
    fun delete(note: Note)

    @Delete
    fun deleteNotebook(notebook: Notebook)

    @Query("DELETE FROM notes")
    fun deleteAllNotes()

    @Query("DELETE FROM notebooks")
    fun deleteAllNotebooks()
}

//@Database(entities = [Note::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun noteDao(): NoteDao
//}