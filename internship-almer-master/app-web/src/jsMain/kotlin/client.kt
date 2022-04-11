import io.almer.BlogClient
import io.almer.Requester
import io.ktor.client.engine.js.*
import io.ktor.http.*
import kotlinx.browser.window

private val url = Url(window.location.href).copy(encodedPath = "", fragment = "")
val apiClient = BlogClient(Requester(Js, url))