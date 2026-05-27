package com.example.routes

import com.example.models.TaskResponse
import com.example.models.Tasks
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.tasksRoutes() {

    route("/tasks") {

        // GET /tasks — все задачи (опционально фильтр по chatId)
        get {
            val chatId = call.request.queryParameters["chatId"]?.toLongOrNull()

            val tasks = transaction {
                val query = if (chatId != null) {
                    Tasks.selectAll().where { Tasks.chatId eq chatId }
                } else {
                    Tasks.selectAll()
                }
                query.orderBy(Tasks.createdAt, SortOrder.DESC).map { row ->
                    TaskResponse(
                        id = row[Tasks.id].value,
                        title = row[Tasks.title],
                        content = row[Tasks.content],
                        chatId = row[Tasks.chatId],
                        done = row[Tasks.done],
                        createdAt = row[Tasks.createdAt].toString()
                    )
                }
            }
            call.respond(tasks)
        }

        // PUT /tasks/{id}/done — отметить выполненной
        put("{id}/done") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")

            transaction {
                Tasks.update({ Tasks.id eq id }) {
                    it[done] = true
                }
            }
            call.respond(HttpStatusCode.OK, "Task marked as done")
        }
    }
}