package io.almer.server.tests

import io.almer.server.mount
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlin.test.assertEquals

class HelloTest {
    @Test
    fun testHello() {
        withTestApplication(Application::mount) {
            handleRequest(HttpMethod.Get, "/hello").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Server is up", response.content)
            }
        }
    }
}