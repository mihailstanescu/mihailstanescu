package io.almer.server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal inline fun <reified R> testNotFound(
    method: HttpMethod,
    path: String,
    body: R? = null,
    actionPath: String = path,
    crossinline extractor: TestApplicationCall.() -> Collection<R>
) {
    withTestApplication(Application::mount) {
        val threadsBeforeAdd = handleRequest(HttpMethod.Get, path).run(extractor)

        val id = 9999

        handleRequest(method, "$actionPath/$id") {
            if (body != null) {
                setBody(body)
            }
        }.apply {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }

        val threadsAfterAdd = handleRequest(HttpMethod.Get, path).run(extractor)

        assertContentEquals(threadsBeforeAdd, threadsAfterAdd)
    }
}


internal inline fun <reified R> testInvalid(
    method: HttpMethod,
    path: String,
    body: R? = null,
    actionPath: String = path,
    crossinline extractor: TestApplicationCall.() -> Collection<R>
) {
    withTestApplication(Application::mount) {
        val threadsBeforeAdd = handleRequest(HttpMethod.Get, path).run(extractor)

        val id = "notAnId"

        handleRequest(method, "$actionPath/$id") {
            if (body != null) {
                setBody(body)
            }
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        val threadsAfterAdd = handleRequest(HttpMethod.Get, path).run(extractor)

        assertContentEquals(threadsBeforeAdd, threadsAfterAdd)
    }
}