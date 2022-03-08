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

    fun testInsertNotes() {
        val testNote1 = Note(
            "This is a test", "test", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )
        val testNote2 = Note(
            "This is another test", "test", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )

        val ids = db!!.insertNotes(listOf(testNote1, testNote2))
        val rets = db!!.selectWithIds(ids)

        assertEquals(2, rets.size)
        assertEquals(testNote1, rets[0])
        assertEquals(testNote2, rets[1])
    }

    fun testSearchWithText() {}
}