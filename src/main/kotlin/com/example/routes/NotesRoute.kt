package com.example.routes

import com.example.models.NoteRequest
import com.example.models.NoteResponse
import com.example.models.Notes
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.notesRoutes() {

    route("/notes") {

        // GET /notes — все заметки
        get {
            val notes = transaction {
                Notes.selectAll().orderBy(Notes.createdAt, SortOrder.DESC).map { row ->
                    NoteResponse(
                        id = row[Notes.id].value,
                        title = row[Notes.title],
                        content = row[Notes.content],
                        createdAt = row[Notes.createdAt].toString()
                    )
                }
            }
            call.respond(notes)
        }

        // GET /notes/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val note = transaction {
                Notes.selectAll().where { Notes.id eq id }.singleOrNull()?.let { row ->
                    NoteResponse(
                        id = row[Notes.id].value,
                        title = row[Notes.title],
                        content = row[Notes.content],
                        createdAt = row[Notes.createdAt].toString()
                    )
                }
            }
            note?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound, "Note not found")
        }

        // POST /notes
        post {
            val req = call.receive<NoteRequest>()
            val created = transaction {
                val newId = Notes.insertAndGetId {
                    it[Notes.title] = req.title
                    it[Notes.content] = req.content
                }.value
                Notes.selectAll().where { Notes.id eq newId }.single().let { row ->
                    NoteResponse(
                        id = row[Notes.id].value,
                        title = row[Notes.title],
                        content = row[Notes.content],
                        createdAt = row[Notes.createdAt].toString()
                    )
                }
            }
            call.respond(HttpStatusCode.Created, created)
        }

        // PUT /notes/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val req = call.receive<NoteRequest>()

            val updated = transaction {
                val count = Notes.update({ Notes.id eq id }) {
                    it[title] = req.title
                    it[content] = req.content
                }
                if (count == 0) return@transaction null
                Notes.selectAll().where { Notes.id eq id }.single().let { row ->
                    NoteResponse(
                        id = row[Notes.id].value,
                        title = row[Notes.title],
                        content = row[Notes.content],
                        createdAt = row[Notes.createdAt].toString()
                    )
                }
            }
            updated?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound, "Note not found")
        }

        // DELETE /notes/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val deleted = transaction {
                Notes.deleteWhere { Notes.id eq id }
            }
            if (deleted > 0) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Note not found")
        }
    }
}