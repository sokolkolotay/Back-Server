package com.example

import com.example.kafka.TaskKafkaConsumer
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureRouting()

    val kafkaServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
    val kafkaTopic = System.getenv("KAFKA_TOPIC") ?: "tasks-created"

    val consumer = TaskKafkaConsumer(kafkaServers, kafkaTopic)

    launch {
        consumer.start()
    }

    monitor.subscribe(ApplicationStopped) {
        consumer.close()
    }
}