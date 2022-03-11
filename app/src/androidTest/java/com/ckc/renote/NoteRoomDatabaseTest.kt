package com.ckc.renote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteRoomDatabaseTest : TestCase() {
    private var noteRoomDatabase: NoteRoomDatabase = NoteRoomDatabase.getDatabase(ApplicationProvider.getApplicationContext<Context>())
    private var dao: NoteDao = noteRoomDatabase.noteDao()
    private val noteName = "test"
    private val bookName = "CS"
    private val testNote = Note(
        "This is a test", noteName, System.currentTimeMillis(),
        System.currentTimeMillis(), 0, bookName
    )
    private val testNotebook = Notebook(
        bookName, 0
    )

    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testGetNoteDao() {
        assertNotNull(noteRoomDatabase.noteDao())
    }

    @Test
    fun testInsertNote() {
        dao.insert(testNote)
        val ret = dao.findByName(noteName)
        assertEquals(testNote, ret)
    }

    @Test
    fun testInsertNotebook() {
        dao.insertNotebook(testNotebook)
        val ret = dao.findNotebookByName(bookName)
        assertEquals(testNotebook, ret)
    }

    @Test
    fun testDeleteNote() {
        dao.insert(testNote)
        val ret = dao.findByName(noteName)
        assertEquals(testNote, ret)
        assertEquals(1, dao.noteExists(noteName))
        dao.delete(testNote)
        assertEquals(0, dao.noteExists(noteName))
    }

    @Test
    fun testDeleteNotebook() {
        dao.insertNotebook(testNotebook)
        val ret = dao.findNotebookByName(bookName)
        assertEquals(testNotebook, ret)
        dao.deleteNotebook(testNotebook)
        val ret2 = dao.findNotebookByName(bookName)
        assertEquals(null, ret2)
    }

    public override fun tearDown() {}
}