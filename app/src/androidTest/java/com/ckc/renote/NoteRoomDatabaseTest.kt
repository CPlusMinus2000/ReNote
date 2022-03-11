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
    private lateinit var noteRoomDatabase: NoteRoomDatabase
    private lateinit var noteDao: NoteDao

    @Before
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

    @After
    public override fun tearDown() {}
}