package com.example.plugins

import com.example.routes.healthRoutes
import com.example.routes.notesRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.configureRouting() {
    // Создаём Prometheus registry
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    // Подключаем метрики JVM
    appMicrometerRegistry.apply {
        ClassLoaderMetrics().bindTo(this)
        JvmMemoryMetrics().bindTo(this)
        JvmGcMetrics().bindTo(this)
        ProcessorMetrics().bindTo(this)
    }

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }

    routing {
        // Эндпоинт для Prometheus — он будет сюда ходить за метриками
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }

        healthRoutes()
        notesRoutes()
    }
}