package io.almer.server.plugins

import io.almer.server.repository.Repository
import io.almer.server.routes.hello
import io.almer.server.routes.users
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureRouting(repository: Repository) {
    routing {
        static("/") {
            resources("static")
            defaultResource("static/index.html")
        }

        // mount the paths
        hello()

        // mount the API paths under /api
        route("/api") {
            users(repository)
            // todo Add the rest of the endpoints
            //    create a separate file (similar to users.kt) for each resource
        }
    }
}