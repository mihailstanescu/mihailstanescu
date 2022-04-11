package io.almer.server

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.assertDoesNotThrow

internal fun TestApplicationRequest.setBody(jsonObject: JsonObject) {
    val s = jsonObject.toString()
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(value = s)
}

internal inline fun <reified T> TestApplicationRequest.setBody(obj: T) {
    val s = Json.encodeToString(obj)
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(value = s)
}

internal val JsonObject.id
    get() = assertDoesNotThrow { this["id"]!!.jsonPrimitive.long }