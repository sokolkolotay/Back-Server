package com.example.kafka

import com.example.models.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

@Serializable
data class TaskMessage(
    val title: String,
    val content: String,
    val chatId: Long
)

class TaskKafkaConsumer(
    bootstrapServers: String,
    private val topic: String
) {
    private val logger = LoggerFactory.getLogger(TaskKafkaConsumer::class.java)

    private val consumer = KafkaConsumer<String, String>(Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, "ktor-api-group")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    })

    suspend fun start() = withContext(Dispatchers.IO) {
        consumer.subscribe(listOf(topic))
        logger.info("Kafka consumer запущен, слушает топик: $topic")

        while (true) {
            val records = consumer.poll(Duration.ofMillis(1000))
            records.forEach { record ->
                try {
                    logger.info("Получено сообщение из Kafka: ${record.value()}")
                    val task = Json.decodeFromString<TaskMessage>(record.value())
                    saveTask(task)
                    logger.info("Задача сохранена в БД: ${task.title}")
                } catch (e: Exception) {
                    logger.error("Ошибка обработки сообщения: ${e.message}")
                }
            }
        }
    }

    private fun saveTask(task: TaskMessage) {
        transaction {
            Tasks.insert {
                it[title] = task.title
                it[content] = task.content
                it[chatId] = task.chatId
            }
        }
    }

    fun close() {
        consumer.close()
    }
}