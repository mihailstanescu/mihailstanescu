package io.almer.server

import io.almer.server.plugins.configureRouting
import io.almer.server.plugins.configureSerialization
import io.almer.server.repository.Repository
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.mount() {
    configureSerialization()
    configureRouting(Repository())
}

fun applicationEngine(): NettyApplicationEngine {

    return embeddedServer(
        Netty,
        port = 3000,
        host = "localhost"
    ) {
        mount()
    }
}

fun main() {
    applicationEngine().start(wait = true)
}
