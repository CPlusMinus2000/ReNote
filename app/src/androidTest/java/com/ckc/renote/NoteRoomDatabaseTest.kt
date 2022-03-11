package com.ckc.renote

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
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
        System.currentTimeMillis(), 1, bookName
    )
    private val testNote2 = Note(
        "This is a test2", "test2", System.currentTimeMillis(),
        System.currentTimeMillis(), 2, bookName
    )
    private val testNote3 = Note(
        "This is a test3", "test3", System.currentTimeMillis(),
        System.currentTimeMillis(), 3, bookName
    )
    private val testNotebook = Notebook(
        bookName, 1
    )

    @Before
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

    @Test
    fun testNoteContents() {
        dao.insert(testNote)
        val ret = dao.findByName(noteName)
        val retContents = ret.contents
        assertEquals(testNote.contents, retContents)
    }

    @Test
    fun testNotebookContents() {
        dao.insertNotebook(testNotebook)
        val ret = dao.findNotebookByName(bookName)
        val returnedName = ret.name
        assertEquals(testNotebook.name, returnedName)
    }

    @Test
    fun testFindNoteByName() {
        dao.insert(testNote)
        dao.insert(testNote2)
        dao.insert(testNote3)
        val ret = dao.findByName(noteName)
        val retContents = ret.contents
        assertTrue(retContents == testNote.contents)
        assertFalse(retContents == testNote2.contents)
        assertFalse(retContents == testNote3.contents)
    }

    @After
    public override fun tearDown() {}
}