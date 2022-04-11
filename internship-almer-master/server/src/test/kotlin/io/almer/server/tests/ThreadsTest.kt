package io.almer.server.tests

import io.almer.server.*
import io.almer.server.testInvalid
import io.almer.server.testNotFound
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*


fun heuristicallyAssertThread(expected: JsonObject, actual: JsonObject) {
    listOf("id", "title", "message", "author").forEach {
        assertEquals(expected[it], actual[it])
    }

    assert(actual.containsKey("created")) {
        "Created"
    }
}

private fun TestApplicationCall.extractThreads(): List<JsonObject> {
    assertEquals(HttpStatusCode.OK, response.status())
    val threadsString = response.content.orEmpty()

    val threads = Json.parseToJsonElement(threadsString)

    val actual = assertDoesNotThrow {
        threads.jsonArray.map { it.jsonObject }
    }

    return actual
}

private fun TestApplicationCall.ensureClean(): List<JsonObject> {
    val actual = extractThreads()
    assertContentEquals(threadsStart.keys, actual)

    return actual
}

class ThreadsTest {
    @Test
    fun testThreadsGet() {
        withTestApplication(Application::mount) {
            val actual = handleRequest(HttpMethod.Get, "/api/threads").ensureClean()
        }
    }

    @Test
    fun testThreadsCreate() {
        withTestApplication(Application::mount) {
            val threadsBeforeAdd = handleRequest(HttpMethod.Get, "/api/threads").ensureClean()

            val newThread = buildJsonObject {
                put("title", "New")
                put("message", "The message")
            }

            val thread = handleRequest(HttpMethod.Post, "/api/threads") {
                setBody(newThread)
            }.run {
                assertEquals(HttpStatusCode.Created, response.status())
                val threadsString = response.content.orEmpty()

                val thread = assertDoesNotThrow { Json.parseToJsonElement(threadsString).jsonObject }

                assertEquals(newThread["title"], thread["title"])
                assertEquals(newThread["message"], thread["message"])

                thread
            }

            val threadsAfterAdd = handleRequest(HttpMethod.Get, "/api/threads").extractThreads()

            assertEquals(threadsBeforeAdd.size + 1, threadsAfterAdd.size, "Incorrect size after add")

            heuristicallyAssertThread(thread, threadsAfterAdd.last())
        }
    }

    @Test
    fun testThreadsPut() {
        withTestApplication(Application::mount) {
            fun updateAndCheck(title: String, message: String) {
                val threadsBeforePut = handleRequest(HttpMethod.Get, "/api/threads").extractThreads()

                val putId = threadsBeforePut.last().id


                val newThread = buildJsonObject {
                    put("title", title)
                    put("message", message)
                }

                handleRequest(HttpMethod.Put, "/api/threads/$putId") {
                    setBody(newThread)
                }.apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                }

                val threadsAfterPut = handleRequest(HttpMethod.Get, "/api/threads").extractThreads()

                assertEquals(threadsBeforePut.size, threadsAfterPut.size, "Incorrect size after put")

                val modified = threadsAfterPut.first { it.id == putId }
                val expect = threadsBeforePut.filter { it.id != putId }
                assertContentEquals(expect, threadsAfterPut.filter { it.id != putId })
                assertEquals(newThread["title"], modified["title"])
                assertEquals(newThread["message"], modified["message"])
                assertEquals(putId, modified.id)
            }


            updateAndCheck("change", "change")
            updateAndCheck("change1", "change")
            updateAndCheck("change1", "change2")
            updateAndCheck("change", "change")
        }
    }

    @Test
    fun testNotFoundThreadsPut() {
        testNotFound(HttpMethod.Put, "/api/threads", buildJsonObject {
            put("title", "title")
            put("message", "message")
        }) {
            extractThreads()
        }
    }

    @Test
    fun testInvalidThreadsPut() {
        testInvalid(HttpMethod.Put, "/api/threads", buildJsonObject {
            put("title", "title")
            put("message", "message")
        }) {
            extractThreads()
        }
    }

    @Test
    fun testThreadsDelete() {
        withTestApplication(Application::mount) {
            val threadsBeforeAdd = handleRequest(HttpMethod.Get, "/api/threads").ensureClean()

            val deleteId = threadsBeforeAdd.last().id

            handleRequest(HttpMethod.Delete, "/api/threads/$deleteId").apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }

            val threadsAfterAdd = handleRequest(HttpMethod.Get, "/api/threads").extractThreads()

            assertEquals(threadsBeforeAdd.size - 1, threadsAfterAdd.size, "Incorrect size after delete")

            val expect = threadsBeforeAdd.filter { it.id != deleteId }
            assertContentEquals(expect, threadsAfterAdd)
        }
    }

    @Test
    fun testNotFoundThreadsDelete() {
        testNotFound(HttpMethod.Delete, "/api/threads") {
            extractThreads()
        }
    }

    @Test
    fun testInvalidThreadsDelete() {
        testInvalid(HttpMethod.Delete, "/api/threads") {
            extractThreads()
        }
    }
}

