package com.example.server


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}

interface NoteRepository : CrudRepository<Note, String> {

    @Query("select * from notesg")
    fun findNotes(): List<Note>
}

@Service
class NoteService(val db: NoteRepository) {

    fun findNotes(): List<Note> = db.findNotes()

    fun post(note: Note): Note {
        return db.save(note)
    }
}

@RestController
class NoteController(val service: NoteService) {
    @GetMapping
    fun index(): List<Note> = service.findNotes()

    @PostMapping
    fun post(@RequestBody note: Note): Note {
        return service.post(note)
    }
}

@Table("NOTESG")
data class Note(
    var name: String,
    var contents: String,
    val creationTime: Long,
    var lastEdited: Long,
    val customOrder: Int,
    val notebookName: String,
    var recording: String? = null,
    @Id var serverId: String? = null
)

