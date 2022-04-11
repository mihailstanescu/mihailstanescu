# API
This project creates a unified API between the clients and the server.

## Task
Implement the API resource objects defined in the [main README](../README.md) (Reply, Thread).Example in
[UserPayload.kt](src/commonMain/kotlin/io/almer/api/User.kt)

Implement the API request objects defined in the [main README](../README.md) (POST Reply, POST Thread). Example in
[UserPayload.kt](src/commonMain/kotlin/io/almer/api/UserPayload.kt)

## Serialization
In order to transfer objects over HTTP we use the JSON serialization and deserialization.

The Kotlinx serialization library makes this very easy. You just have to add the dependency (I already did that for you)
and annotate the objects you want to be serializable with `@kotlinx.serialization.Serializable`.

We use the `data class` for these objects because Kotlin automatically implements `toString` and `equals`for them.

##Implementation the API resource objects

 package io.almer.api

  @kotlinx.serialization.Serializable

  data class USER
  (
  val Reply: String,
  val Thread:Boolean 
 )
  
##Implementation API request

 package io.almer.api

 @kotlinx.serialization.Serializable
 data class POS
 (
 val PostReply: String,
 val PostThread: Boolean
 )