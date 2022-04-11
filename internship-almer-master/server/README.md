# Backend
This projects keeps the server code and the database.

## Database
For the database we will be using the [Sqldelight framework](https://cashapp.github.io/sqldelight/jvm_sqlite/).
This framework will generate a typesafe API from our SQL queries, so the compiler can verify our programs' correctness.

The framework is all ready set up in the build process, so you should not have any problems with that.

The Sqldelight plugin should deal with automatic refresh of the generated classes. If it does not you can run
the following gradle tasks:
* `:server:generateSqlDelightInterface` -> will build the model
* `:server:build` -> will build the whole project

You can find an example of how to build a file in [User.sq](src/main/sqldelight/io/almer/db/User.sq).

You need to create the rest of the tables, each in a separate `.sq` file under
[src/main/sqldelight/io/almer/db](src/main/sqldelight/io/almer/db).

### Start data
Because I don't know what your database will look like I need you to inject some start data into your database for me :)
The structure should look like this:

All Reply resources have the same created date for simplicity (1649079481).
Authors alternate by John, Jane, Anonymous for each resource type. That means

* `id % 3 == 1` -> John 
* `id % 3 == 2` -> Jane 
* `id % 3 == 0` -> Anonymous

We have the following start data:

* Thread(id: 1, title: Alpha, created: 1649079474, message: The first one, author: User(John))
    * Reply(id: 1, message: R1, author: User(John)) -> Reply to T1
    * Reply(id: 2, message: R2, author: User(Jane)) -> Reply to T1
        * Reply(id: 3, message: R3, author: User(Anonymous)) -> Reply to R2
* Thread(id: 2, title: Beta, created: 1649079480, message: The second one, author: User(Jane))
    * Reply(id: 4, message: R4, author: User(John)) -> Reply to T2
        * Reply(id: 5, message: R5, author: User(Jane)) -> Reply to R4
            * Reply(id: 6, message: R6, author: User(Anonymous)) -> Reply to R5
                * Reply(id: 7, message: R7, author: User(John)) -> Reply to R6
                * Reply(id: 8, message: R8, author: User(Jane)) -> Reply to R6
* Thread(id: 3, title: Charlie, created: 1649079480, message: The third one, author: User(Anonymous))

If there are any problems, take a look in
[src/test/kotlin/io/almer/server/startData.kt](src/test/kotlin/io/almer/server/startData.kt) at the `threadsStart`
variable.

If there are still discrepancies, tests are kings, just make em pass.

**Do this as you define each table, as you need them to pass even 1 test**

## Test
You can find the tests under [src/test/kotlin/io/almer/server](src/test/kotlin/io/almer/server). They are already
written, so you should not have a lot to do with them.

## Code
Finally coding...

### Structure
* `plugins` -> Ktor framework plugins. Add features to our http framework.
  * `Routing.kt` -> Routing plugin. Mounts the routes found in `routes`
  * `Serialization.kt` -> Installs the JSON serializer. Nothing to do here
* `repository` -> Abstracts the database layer away. Implement database interactions here.
  * `RepositoryBase.kt` -> Base class for Repositories.
  * `UserRepository.kt` -> Repository for User
  * `Repository.kt` -> Class to hold all the Repositories(User, Threads, etc)
* `routes` -> Defines Kotlin extension functions to be used for handling the HTTP ReST interface. Define the missing
                `threads` and `replies` endpoints here
  * `users.kt` -> Handler for the `users` endpoint
* `ApiMapper.kt` -> Defines Kotlin extension functions to convert between the API model and the database model.
                      Careful, the name `Thread` is included by default in the scope of a file and referees to the Java
                      Thread, so you specify it's full path or import it to use it.
* `Application.kt` -> Handle the server open and stuff. Nothing to do here

### TODOs
I have added TODOs everywhere you need to modify.

### Run the tests
To run the tests run the `:server:test`. It is recommended to run in the IDE, so you get a nice interface.
You can also easily run individual tests, check [test-interface.png](../test-interface.png)
You can also find the `.html` output of the test results at
[build/reports/tests/test/index.html](build/reports/tests/test/index.html) if you run from the gradle cli.


##plugins
package io.almer.server.plugins

import io.almer.server.repository.Repository
import io.almer.server.routes.hello
import io.almer.server.routes.users
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureRouting(repository: Repository) {
routing {
static("/") {
resources("static")
defaultResource("static/index.html")
}

        // mount the paths
        hello()

        // mount the API paths under /api
        route("/api") {
            users(repository)
            // todo Add the rest of the endpoints
            //    create a separate file (similar to users.kt) for each resource
        }
    }
}

#Serialization
package io.almer.server.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*

fun Application.configureSerialization() {
install(ContentNegotiation) {
json()
}
}

#repository

   #RepositoryBase
package io.almer.server.repository

import io.almer.db.BlogDB

/**
* Base class to grant access the Repository and db API
  */
  abstract class RepositoryBase(protected val db: BlogDB, protected val repository: Repository) {
  }
   #UserRepository
package io.almer.server.repository

import io.almer.api.User
import io.almer.api.UserPayload
import io.almer.db.BlogDB
import io.almer.server.toApi

/**
* Handles access to the User resource. Hides the db implementation
  */
  class UserRepository(db: BlogDB, repository: Repository) : RepositoryBase(db, repository) {
  /**
    * Select all users
      */
      fun selectAll(): List<User> {
      // the db interface is generated automatically by Sqldelight
      // we convert to an API object to hide the underlying implementation of the db
      return db.userQueries.selectAll().executeAsList().map { it.toApi() }
      }

  /**
    * Select one user
      */
      fun selectOne(id: Long): User {
      return db.userQueries.selectOne(id).executeAsOne().toApi()
      }

  /**
    * Delete one user
      */
      fun deleteOne(id: Long): Boolean {
      // also need the number of rows delete, to assess if we actually had the row,
      // so we use a transaction. The Sqldelight API is pretty nice, you just open the transaction
      // and give it a lambda, and it will handle the commit by itself
      val rows =
      db.userQueries.transactionWithResult<Long> {
      db.userQueries.deleteOne(id)
      val deletedRows = db.userQueries.selectChanges().executeAsOne()

               deletedRows
           }

      // see if any rows were affected
      return rows != 0L
      }

  fun createOne(userPayload: UserPayload): User {
  // we need the last insert ID, so we do another transaction
  // we know this is not the most efficient way
  val insertId =
  db.userQueries.transactionWithResult<Long> {
  db.userQueries.insertOne(userPayload.firstName, userPayload.lastName, userPayload.admin)
  val insertId = db.userQueries.lastInsertId().executeAsOne()

               insertId
           }

       return User(insertId, userPayload.firstName, userPayload.lastName, userPayload.admin)
  }
  }
   #Repository
package io.almer.server.repository

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.almer.db.BlogDB

class Repository(driver: SqlDriver) {
init {
BlogDB.Schema.create(driver)
}

    private val db = BlogDB(driver)

    val userRepository = UserRepository(db, this)
    // todo Add the rest of the repositories
    //    create a separate repository class for each resource (eg ThreadRepository)

    constructor(): this(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
}

#routes
#Hello
package io.almer.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*


fun Route.hello() {
get("/hello") {
call.respondText("Server is up")
}
}
#users
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

#ApiMapper

package io.almer.server

import io.almer.api.User
import io.almer.server.repository.Repository

fun io.almer.db.User.toApi() = User(this.id, this.firstName, this.lastName, this.admin)


#Application
package io.almer.server

import io.almer.server.plugins.configureRouting
import io.almer.server.plugins.configureSerialization
import io.almer.server.repository.Repository
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.mount() {
configureSerialization()
configureRouting(Repository())
}

fun applicationEngine(): NettyApplicationEngine {

    return embeddedServer(
        Netty,
        port = 3000,
        host = "localhost"
    ) {
        mount()
    }
}

fun main() {
applicationEngine().start(wait = true)
}


