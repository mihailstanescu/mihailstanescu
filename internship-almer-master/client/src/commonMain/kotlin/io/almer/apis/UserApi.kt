package io.almer.apis

import io.almer.Requester
import io.almer.api.User
import io.ktor.client.request.*
import org.lighthousegames.logging.logging

class UserApi(requester: Requester) : ApiClient(requester) {
    companion object {
        private val Log = logging("UserApi")
    }

    // Kotlin has no forced catch Exception like Java. If a function can fail we should use a special type to mark this
    //    if we just want to differentiate between Success and Error (without offering a differentiation between the errors)
    //    we can use the std class Result. `runCatching` is a shorthand for a try-catch block that wraps the result in
    //    the result class, but I will use the explicit Java like version to not confuse you
    suspend fun getUser(): Result<List<User>> {
        Log.d { "Getting all the users" }

        try {
            val response = requester.client.get<List<io.almer.api.User>>(requester.baseUrl.copy(encodedPath = "/api/user"))
            Log.d { "Successfully retried $response" }

            return Result.success(response)
        } catch (e: Throwable) {
            Log.e(e) {"Unable to retrieve the users"}
            return Result.failure(e)
        }
    }
}