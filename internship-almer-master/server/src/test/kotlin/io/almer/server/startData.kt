package io.almer.server

import io.almer.api.User
import kotlinx.serialization.json.*


fun threadJsonObject(
    id: Long,
    title: String,
    created: Long,
    message: String,
    author: User? = null
): JsonObject {
    return buildJsonObject {
        put("id", id)
        put("title", title)
        put("created", created)
        put("author", Json.encodeToJsonElement(author))
        put("message", message)
    }
}

fun repliesJsonObject(
    id: Long,
    created: Long,
    message: String,
    author: User? = null
): JsonObject {
    return buildJsonObject {
        put("id", id)
        put("created", created)
        put("author", Json.encodeToJsonElement(author))
        put("message", message)
    }
}

private val thomas = User(1, "Thomas", "Anderson", true)
private val john = User(2, "John", "Doe", false)
private val jane = User(3, "Jane", "Doe", false)

val startsUsers = listOf(
    thomas,
    john,
    jane,
)


data class Reply(
    private val _jsonObject: JsonObject,
    val children: List<Reply> = emptyList()
) {
    val jsonObject
        get() = buildJsonObject {
            _jsonObject.forEach {
                put(it.key, it.value)
            }

            put("replies", children.size)
        }

    constructor(jsonObject: JsonObject, vararg children: Reply) : this(jsonObject, children.toList())
}

const val replyTS = 1649079481L

val threadsStart = mapOf<JsonObject, List<Reply>>(
    threadJsonObject(1, "Alpha", 1649079474, "The first one", startsUsers[1]) to listOf(
        Reply(repliesJsonObject(1, replyTS, "R1", john)),
        Reply(repliesJsonObject(2, replyTS, "R2", jane), Reply(repliesJsonObject(3, replyTS, "R3"))),
    ),
    threadJsonObject(2, "Beta", 1649079480, "The second one", startsUsers[2]) to listOf(
        Reply(
            repliesJsonObject(4, replyTS, "R4", john),
            Reply(
                repliesJsonObject(5, replyTS, "R5", jane),
                Reply(
                    repliesJsonObject(6, replyTS, "R6"),
                    Reply(repliesJsonObject(7, replyTS, "R7", john)),
                    Reply(repliesJsonObject(8, replyTS, "R8", jane)),
                ),
            )
        ),
    ),
    threadJsonObject(3, "Charlie", 1649079480, "The third one") to listOf(
    ),
)
