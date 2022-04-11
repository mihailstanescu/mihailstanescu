package io.almer.server.tests

import io.almer.api.User
import io.almer.api.UserPayload
import io.almer.server.*
import io.almer.server.testInvalid
import io.almer.server.testNotFound
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.*

private fun TestApplicationCall.extractUsers(): List<User> {
    assertEquals(HttpStatusCode.OK, response.status())
    val usersString = response.content.orEmpty()

    return Json.decodeFromString(usersString)
}

class UsersTest {
    @Test
    fun testUsersGet() {
        withTestApplication(Application::mount) {
            handleRequest(HttpMethod.Get, "/api/users").apply {
                assertContentEquals(startsUsers, extractUsers())
            }
        }
    }

    @Test
    fun testUsersCreate() {
        withTestApplication(Application::mount) {
            val usersBeforeAdd = handleRequest(HttpMethod.Get, "/api/users").extractUsers()

            val newUser = UserPayload("gogu", "gogu", false)
            val user = handleRequest(HttpMethod.Post, "/api/users") {
                setBody(newUser)
            }.run {
                assertEquals(HttpStatusCode.Created, response.status())
                val usersString = response.content.orEmpty()

                val returnedUser = Json.decodeFromString<User>(usersString)

                assertEquals(newUser.firstName, returnedUser.firstName)
                assertEquals(newUser.lastName, returnedUser.lastName)
                assertEquals(newUser.admin, returnedUser.admin)

                returnedUser
            }

            val usersAfterAdd = handleRequest(HttpMethod.Get, "/api/users").extractUsers()

            assertEquals(usersBeforeAdd.size + 1, usersAfterAdd.size, "Incorrect size after add")

            assertEquals(user, usersAfterAdd.last())
        }
    }

    @Test
    fun testUsersDelete() {
        withTestApplication(Application::mount) {
            val usersBeforeAdd = handleRequest(HttpMethod.Get, "/api/users").extractUsers()

            val deleteId = usersBeforeAdd.last().id

            handleRequest(HttpMethod.Delete, "/api/users/$deleteId").apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }

            val usersAfterAdd = handleRequest(HttpMethod.Get, "/api/users").extractUsers()

            assertEquals(usersBeforeAdd.size - 1, usersAfterAdd.size, "Incorrect size after delete")

            val expect = usersBeforeAdd.filter { it.id != deleteId }
            assertContentEquals(expect, usersAfterAdd)
        }
    }

    @Test
    fun testNotFoundUsersDelete() {
        testNotFound(HttpMethod.Delete, "/api/users") {
            extractUsers()
        }
    }

    @Test
    fun testInvalidUsersDelete() {
        testInvalid(HttpMethod.Delete, "/api/users") {
            extractUsers()
        }
    }
}

