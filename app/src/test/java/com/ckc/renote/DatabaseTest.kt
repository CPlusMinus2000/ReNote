package com.ckc.renote

import junit.framework.TestCase

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

class DatabaseTest : TestCase() {
    private var db: RawDatabase? = null

    public override fun setUp() {
        super.setUp()
        db = RawDatabase()
        db!!.init("jdbc:sqlite:test.db")
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

    fun testGetNoteByName() {
        val randomName = getRandomString(10)
        val testNote = Note(
            "This is a test", randomName, System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )

        db!!.insertNote(testNote)
        assert(db!!.checkIfNameExists(randomName))
        val ret = db!!.getNoteByName(randomName)
        assertEquals(testNote, ret)
    }

    fun testSearchWithText() {
        val randomText = getRandomString(10)
        val testNote1 = Note(
            "This is a test with $randomText", "test", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )
        val testNote2 = Note(
            "This is another test with $randomText", "test2", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )
        val testNote3 = Note(
            "This is a tset", "test3", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )

        db!!.insertNotes(listOf(testNote1, testNote2))
        val rets = db!!.searchWithText(randomText)

        assertEquals(2, rets.size)
        assertEquals(testNote1, rets[0])
        assertEquals(testNote2, rets[1])
    }

    fun testExecuteCustomQuery() {
        val testTitle = getRandomString(10)
        val testNote1 = Note(
            "This is a test", testTitle, System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )
        val testNote2 = Note(
            "This is another test", "lolol", System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )
        val testNote3 = Note(
            "This is a tset", testTitle, System.currentTimeMillis(),
            System.currentTimeMillis(), null
        )

        db!!.insertNotes(listOf(testNote1, testNote2, testNote3))
        val rets = db!!.executeCustomQuery("SELECT * FROM Notes WHERE name = '$testTitle' order by NoteID asc;")

        val titles = mutableListOf<String>()
        while (rets!!.next()) {
            titles.add(rets.getString("name"))
        }
        assertEquals(2, titles.size)
        assertEquals(testNote1.name, titles[0])
        assertEquals(testNote3.name, titles[0])
    }
}