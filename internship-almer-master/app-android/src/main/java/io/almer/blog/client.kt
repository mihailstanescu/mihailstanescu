package io.almer.blog

import io.almer.BlogClient
import io.almer.Requester
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*


// you can access localhost on an Android emulator at 10.0.2.2
// if you want a physical device, you need to export your server to the public interface and put in it's ip
private val url = Url("http://10.0.2.2:3000")
val apiClient = BlogClient(Requester(CIO, url))