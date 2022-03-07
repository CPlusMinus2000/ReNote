package com.ckc.renote

import junit.framework.TestCase

class DatabaseTest : TestCase() {
    private var db: Database? = null

    public override fun setUp() {
        super.setUp()
        db = Database("jdbc:sqlite:test.db")
    }

    public override fun tearDown() {
        db!!.close()
    }

    fun testInsertNote() {
        val testNote = Note(
            "This is a test", "test", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )

        val id = db!!.insertNote(testNote)
        val ret = db!!.selectWithId(id)
        assertEquals(testNote, ret)
    }

    fun testInsertNotes() {}

    fun testSearchWithText() {}
}