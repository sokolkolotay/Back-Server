package com.example.plugins

import com.example.routes.healthRoutes
import com.example.routes.notesRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }

    routing {
        healthRoutes()
        notesRoutes()
    }
}