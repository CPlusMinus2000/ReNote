package com.ckc.renote

import android.util.Log
import java.sql.*

class RawDatabase() {
    // Interface class for accessing the database
    private var conn: Connection? = null
    private var tableName: String = "Notes"

    /* init {
        Log.d("Database", "Database getting created")
        try {
            conn = DriverManager.getConnection(url)
        } catch (ex: SQLException) {
            println(ex.message)
        }
        Log.d("lololXDXDXDXD", conn.toString())
        try {
            if (conn != null) {
                val creation = "create table if not exists $tableName (" +
                        "noteID integer primary key, " +
                        "contents text not null, " +
                        "name text not null, " +
                        "creationTime int not null, " +
                        "lastEdited int not null, " +
                        "customOrder int);"

                val request = conn!!.createStatement()
                val results = request.executeUpdate(creation)
                println(results)
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }
    } */

    fun init(url: String, tableName: String = "Notes") {
        try {
            this.tableName = tableName
            Log.d("Database", "Database getting created")
            conn = DriverManager.getConnection(url)
            Log.d("lololXDXDXDXD", conn.toString())
            val creation = "create table if not exists $tableName (" +
                    "noteID integer primary key, " +
                    "contents text not null, " +
                    "name text not null, " +
                    "creationTime int not null, " +
                    "lastEdited int not null, " +
                    "customOrder int);"

            val request = conn!!.createStatement()
            val results = request.executeUpdate(creation)
            println(results)
        } catch (ex: SQLException) {
            ex.message?.let { Log.d("lololXDException", it) }
            println(ex.message)
        }
    }

    // Returns the ID of the inserted note
    fun insertNote(note: Note): Int {
        var id = 0
        val insertion = "insert into $tableName(" +
                "contents, name, creationTime, lastEdited, customOrder) values (" +
                "\"${note.contents}\", \"${note.name}\", ${note.creationTime}, ${note.lastEdited}, ${note.customOrder}" +
                ");"

        try {
            if (conn != null) {
                val query = conn!!.createStatement()
                query.executeUpdate(insertion)

                val idQuery = conn!!.createStatement()
                val idRes = idQuery.executeQuery("select last_insert_rowid() from $tableName limit 1;")
                while (idRes.next()) {
                    id = idRes.getInt("last_insert_rowid()")
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return id
    }

    fun insertNotes(notes: List<Note>): MutableList<Int> {
        val ids = mutableListOf<Int>()
        try {
            if (conn != null) {
                for (note in notes) {
                    val insertion = "insert into $tableName(" +
                            "contents, name, creationTime, lastEdited, customOrder) values (" +
                            "\"${note.contents}\", \"${note.name}\", ${note.creationTime}, " +
                            "${note.lastEdited}, ${note.customOrder});"

                    val query = conn!!.createStatement()
                    query.executeUpdate(insertion)

                    val idQuery = conn!!.createStatement()
                    val idRes = idQuery.executeQuery("select last_insert_rowid() from $tableName limit 1;")
                    while (idRes.next()) {
                        ids.add(idRes.getInt("last_insert_rowid()"))
                    }
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return ids
    }

    fun checkIfNameExists(name: String): Boolean {
        var exists = false
        try {
            if (conn != null) {
                val query = conn!!.createStatement()
                val results = query.executeQuery("select * from $tableName where name = \"$name\";")
                while (results.next()) {
                    exists = true
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return exists
    }

    fun getNoteByName(name: String): Note? {
        var note: Note? = null
        try {
            if (conn != null) {
                val query = conn!!.createStatement()
                val res = query.executeQuery("select * from $tableName where name = \"$name\" limit 1;")
                while (res.next()) {
                    note = Note(
                            res.getString("contents"),
                            res.getString("name"),
                            res.getLong("creationTime"),
                            res.getLong("lastEdited"),
                            if (res.getObject("customOrder") == null) null else res.getInt("customOrder")
                    )
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return note
    }

    fun selectWithId(id: Int): Note? {
        var note: Note? = null
        try {
            if (conn != null) {
                val sql = "select * from $tableName where noteID = $id"
                val query = conn!!.createStatement()
                val results = query.executeQuery(sql)
                while (results.next()) {
                    note = Note(
                        results.getString("contents"),
                        results.getString("name"),
                        results.getLong("creationTime"),
                        results.getLong("lastEdited"),
                        if (results.getObject("customOrder") == null) null else results.getInt("customOrder")
                    )
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return note
    }

    fun selectWithIds(ids: List<Int>): List<Note> {
        val notes = mutableListOf<Note>()
        try {
            if (conn != null) {
                var sql = "select * from $tableName where noteID in ("
                for (id in ids) {
                    sql += "$id,"
                }
                sql = sql.substring(0, sql.length - 1)
                sql += ")"

                val query = conn!!.createStatement()
                val results = query.executeQuery(sql)
                while (results.next()) {
                    notes.add(
                        Note(
                            results.getString("contents"),
                            results.getString("name"),
                            results.getLong("creationTime"),
                            results.getLong("lastEdited"),
                            if (results.getObject("customOrder") == null) null else results.getInt("customOrder")
                        )
                    )
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return notes
    }

    // Get all notes containing a substring as contents
    fun searchWithText(text: String): MutableList<Note> {
        val searchResults = mutableListOf<Note>()
        try {
            if (conn != null) {
                val sql = "select * from $tableName where contents like \"%$text%\" order by noteID asc;"

                val query = conn!!.createStatement()
                val results = query.executeQuery(sql)
                while (results.next()) {
                    searchResults.add(Note(
                        results.getString("contents"),
                        results.getString("name"),
                        results.getLong("creationTime"),
                        results.getLong("lastEdited"),
                        if (results.getObject("customOrder") == null) null else results.getInt("customOrder")
                    ))
                }
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return searchResults
    }

    fun executeCustomQuery(sql: String): ResultSet? {
        var res: ResultSet? = null
        try {
            if (conn != null) {
                res = conn!!.createStatement().executeQuery(sql)
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }

        return res
    }

    fun close() {
        try {
            if (conn != null) {
                conn!!.close()
            }
        } catch (ex: SQLException) {
            println(ex.message)
        }
    }
}