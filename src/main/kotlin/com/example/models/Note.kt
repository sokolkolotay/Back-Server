package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Таблица в БД
object Notes : IntIdTable("notes") {
    val title = varchar("title", 255)
    val content = text("content")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

// DTO для запросов/ответов
@Serializable
data class NoteRequest(
    val title: String,
    val content: String
)

@Serializable
data class NoteResponse(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: String
)

// Таблица задач
object Tasks : IntIdTable("tasks") {
    val title = varchar("title", 255)
    val content = text("content")
    val chatId = long("chat_id")
    val done = bool("done").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

@Serializable
data class TaskResponse(
    val id: Int,
    val title: String,
    val content: String,
    val chatId: Long,
    val done: Boolean,
    val createdAt: String
)