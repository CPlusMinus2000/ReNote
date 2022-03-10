package com.ckc.renote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteRoomDatabaseTest : TestCase() {
    private lateinit var noteRoomDatabase: NoteRoomDatabase
    private lateinit var noteDao: NoteDao

    public override fun setUp() {
        super.setUp()
        val context = ApplicationProvider.getApplicationContext<Context>()
        noteRoomDatabase = NoteRoomDatabase.getDatabase(context)
        noteDao = noteRoomDatabase.noteDao()
    }

    @Test
    fun testGetNoteDao() {
        assertNotNull(noteRoomDatabase.noteDao())
    }

    public override fun tearDown() {}
}