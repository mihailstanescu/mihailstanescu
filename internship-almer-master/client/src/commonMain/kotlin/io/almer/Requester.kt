package io.almer

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.features.json.*
import io.ktor.http.*

class Requester(
    engine: HttpClientEngine,
    val baseUrl: Url
) {
    val client = HttpClient(engine) {
        install(JsonFeature)
    }

    // secondary constructor, for easy init
    constructor(engineFactory: HttpClientEngineFactory<*>, baseUrl: Url) : this(engineFactory.create(), baseUrl)
}