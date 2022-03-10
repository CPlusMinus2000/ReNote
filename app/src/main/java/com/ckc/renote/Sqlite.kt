package com.ckc.renote


import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase


class Sqlite(url: String, private val tableName: String = "Notes") {
    private val db = openOrCreateDatabase(url, null)

    fun createTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS $tableName (id INTEGER PRIMARY KEY AUTOINCREMENT, contents TEXT, name TEXT, creationTime TEXT, updateTime TEXT)")
    }

    fun insert(title: String, content: String, createTime: String, updateTime: String) {
        db.execSQL("INSERT INTO $tableName (title, content, createTime, updateTime) VALUES (?, ?, ?, ?)", arrayOf(title, content, createTime, updateTime))
    }

    fun update(id: Int, title: String, content: String, createTime: String, updateTime: String) {
        db.execSQL("UPDATE $tableName SET title = ?, content = ?, createTime = ?, updateTime = ? WHERE id = ?", arrayOf(title, content, createTime, updateTime, id))
    }

    fun delete(id: Int) {
        db.execSQL("DELETE FROM $tableName WHERE id = ?", arrayOf(id))
    }

    fun query(name: String): Note {
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE name = ?", arrayOf(name))
        cursor.moveToFirst()
        val name = cursor.getString(cursor.getColumnIndex("name"))
        val contents = cursor.getString(cursor.getColumnIndex("contents"))
        val createTime = cursor.getLong(cursor.getColumnIndex("createTime"))
        val updateTime = cursor.getLong(cursor.getColumnIndex("updateTime"))
        cursor.close()
        return Note(contents, name, createTime, updateTime, null)
    }
}