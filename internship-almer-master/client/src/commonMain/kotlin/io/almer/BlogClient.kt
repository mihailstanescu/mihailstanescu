package io.almer

import io.almer.apis.UserApi
import org.lighthousegames.logging.logging

class BlogClient(requester: Requester) {
    companion object {
        private val Log = logging("BlogClient")
    }

    val userApi = UserApi(requester)
}