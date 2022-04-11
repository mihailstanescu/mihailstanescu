package io.almer.server.routes

import io.almer.api.UserPayload
import io.almer.server.repository.Repository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.lighthousegames.logging.logging

private val Log = logging("users")

/**
 * Mounts the `users` endpoints
 */
fun Route.users(repository: Repository) {
    // mount a GET handler on /users path
    get("/users") {
        // call the repository
        val users = repository.userRepository.selectAll()
        // answer the HTTP call. The JSON plugin automatically handles serialization
        call.respond(users)
    }

    // mount a DELETE handler on /users/:id path
    // the {id} denotes a path variable
    delete("/users/{id}") {
        // get the id variable from the path
        // the name of the variable must match the one give in the string path (/users/{id})
        // if you are using the `val smth: Type by call.parameters` construct
        // this construct will automatically respond with BadRequest in case the :id can not
        // be converted to the Type (in our case Long)
        // if you need more info look at https://ktor.io/docs/requests.html#request_information
        val id: Long by call.parameters

        // perform the delete, and check if it was successful
        val found = repository.userRepository.deleteOne(id)

        if (found) {
            // if it was successful, respond with NoContent
            call.response.status(HttpStatusCode.NoContent)
        } else {
            // if nothing was deleted, respond with NotFound
            call.response.status(HttpStatusCode.NotFound)
        }
    }

    // here we mount a Handler for POST /users that needs to receive a [UserPayload] data type
    // as the body. This is then passed as the argument to the handler
    post<UserPayload>("/users") { userCreate ->

        // create the new User
        val user = repository.userRepository.createOne(userCreate)

        // return it
        call.respond(HttpStatusCode.Created, user)
    }
}