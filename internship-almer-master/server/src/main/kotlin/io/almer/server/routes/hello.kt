package io.almer.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*


fun Route.hello() {
    get("/hello") {
        call.respondText("Server is up")
    }
}