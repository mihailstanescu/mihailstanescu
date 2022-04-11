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

private fun TestApplicationCall.extractReplies(): List<JsonObject> {
    assertEquals(HttpStatusCode.OK, response.status())
    val repliesString = response.content.orEmpty()

    val replies = Json.parseToJsonElement(repliesString)

    val actual = assertDoesNotThrow {
        replies.jsonArray.map { it.jsonObject }
    }

    return actual
}

class RepliesTest {
    @Test
    fun testRepliesGet() {
        withTestApplication(Application::mount) {
            fun asses(id: Int) {
                val actual = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                assertContentEquals(threadsStart.values.elementAt(id - 1).map { it.jsonObject }, actual)
            }

            asses(1)
            asses(2)
            asses(3)
        }
    }

    @Test
    fun testRepliesGetById() {
        withTestApplication(Application::mount) {
            fun asses(reply: Reply) {
                val actual = handleRequest(HttpMethod.Get, "/api/replies/${reply.jsonObject.id}").extractReplies()

                assertContentEquals(reply.children.map { it.jsonObject }, actual)
            }

            threadsStart.values.forEach {
                it.forEach { asses(it) }
            }
        }
    }

    @Test
    fun testRepliesCreate() {
        withTestApplication(Application::mount) {
            fun test(id: Int) {
                val repliesBeforeAdd = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                val newReply = buildJsonObject {
                    put("message", "The message")
                }

                val thread = handleRequest(HttpMethod.Post, "/api/threads/$id/replies") {
                    setBody(newReply)
                }.run {
                    assertEquals(HttpStatusCode.Created, response.status())
                    val repliesString = response.content.orEmpty()

                    val thread = assertDoesNotThrow { Json.parseToJsonElement(repliesString).jsonObject }

                    assertEquals(newReply["message"], thread["message"])

                    thread
                }

                val repliesAfterAdd = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                assertEquals(repliesBeforeAdd.size + 1, repliesAfterAdd.size, "Incorrect size after add")

                heuristicallyAssertThread(thread, repliesAfterAdd.last())
            }

            test(1)
            test(2)
            test(3)
        }
    }

    @Test
    fun testRepliesPut() {
        withTestApplication(Application::mount) {
            fun test(id: Int) {
                fun updateAndCheck(message: String) {
                    val repliesBeforePut = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                    val putId = repliesBeforePut.last().id


                    val newReply = buildJsonObject {
                        put("message", message)
                    }

                    handleRequest(HttpMethod.Put, "/api/replies/$putId") {
                        setBody(newReply)
                    }.apply {
                        assertEquals(HttpStatusCode.NoContent, response.status())
                    }

                    val repliesAfterPut = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                    assertEquals(repliesBeforePut.size, repliesAfterPut.size, "Incorrect size after put")

                    val modified = repliesAfterPut.first { it.id == putId }
                    val expect = repliesBeforePut.filter { it.id != putId }
                    assertContentEquals(expect, repliesAfterPut.filter { it.id != putId })
                    assertEquals(newReply["message"], modified["message"])
                    assertEquals(putId, modified.id)
                }


                updateAndCheck("change")
                updateAndCheck("change1")
            }

            test(1)
            test(2)
        }
    }

    @Test
    fun testNotFoundRepliesPut() {
        fun test(id: Int) {
            testNotFound(HttpMethod.Put, "/api/threads/$id/replies", buildJsonObject {
                put("message", "message")
            }) {
                extractReplies()
            }
        }

        test(1)
        test(2)
        test(3)
    }

    @Test
    fun testInvalidRepliesPut() {
        fun test(id: Int) {
            testInvalid(HttpMethod.Put, "/api/threads/$id/replies", buildJsonObject {
                put("message", "message")
            }, "/api/replies") {
                extractReplies()
            }
        }
        test(1)
        test(2)
        test(3)
    }

    @Test
    fun testRepliesDelete() {
        withTestApplication(Application::mount) {
            fun test(id: Int) {
                val repliesBeforeAdd = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                val deleteId = repliesBeforeAdd.last().id

                handleRequest(HttpMethod.Delete, "/api/replies/$deleteId").apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                }

                val repliesAfterAdd = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                assertEquals(repliesBeforeAdd.size - 1, repliesAfterAdd.size, "Incorrect size after delete")

                val expect = repliesBeforeAdd.filter { it.id != deleteId }
                assertContentEquals(expect, repliesAfterAdd)
            }

            test(1)
            test(2)
        }
    }

    @Test
    fun testNotFoundRepliesDelete() {
        fun test(id: Int) {
            testNotFound(HttpMethod.Delete, "/api/threads/$id/replies", actionPath = "/api/replies") {
                extractReplies()
            }
        }
        test(1)
        test(2)
        test(3)
    }

    @Test
    fun testInvalidRepliesDelete() {
        fun test(id: Int) {
            testInvalid(HttpMethod.Delete, "/api/threads/$id/replies", actionPath = "/api/replies") {
                extractReplies()
            }
        }
        test(1)
        test(2)
        test(3)
    }


    @Test
    fun testRepliesDeletedAfterThreadDelete() {
        fun test(id: Int) {
            withTestApplication(Application::mount) {
                val repliesBeforeAdd = handleRequest(HttpMethod.Get, "/api/threads/$id/replies").extractReplies()

                handleRequest(HttpMethod.Delete, "/api/threads/$id").apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                }

                repliesBeforeAdd.forEach {
                    val deleteId = it.id
                    handleRequest(HttpMethod.Delete, "/api/replies/$deleteId").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                    }
                }
            }
        }

        test(1)
        test(2)
    }

    @Test
    fun testRepliesDeletedAfterReplyDelete() {
            withTestApplication(Application::mount) {
                handleRequest(HttpMethod.Delete, "/api/replies/4").apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                }
                val left = handleRequest(HttpMethod.Get, "/api/threads/2/replies").extractReplies()

                assertEquals(0, left.size)

                Thread.sleep(3000)

                for (i in 5..8) {
                    handleRequest(HttpMethod.Delete, "/api/replies/$i").apply {
                        assertEquals(HttpStatusCode.NotFound, response.status(), "Failed to delete $i")
                    }
                }

                val before = handleRequest(HttpMethod.Get, "/api/threads/1/replies").extractReplies()

                assert(before.isNotEmpty())
            }
    }
}

