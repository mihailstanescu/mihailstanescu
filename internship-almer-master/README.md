# Almer Blog

This exercise should have consisted of building a very small blog application. With:
* Backend
* Database
* Web App
* Android App

Communication between the client side and server side is done using a ReST API.

Since it would have taken you too much time (and it felt kinda shitty from our side to ask this of you),
we will just focus and the Backend and Database since you should have more experience with them from school. Almost all
the knowledge needed to solve this exercises is also needed when developing Android applications, so it's a win win :).
We have removed these subprojects from the build to be sure they don't cause you problems.

This exercise also gives you a very short peek of how modern mobile and web applications work.

Keep in mind that this is for educational purposes only. We chose a more simplistic design, an in-memory Sqlite database
(since you should know SQL by now and there is no setup) and some libraries that we don't normally use. We also chose to
not use some features of some libraries and Kotlin just to make it more easy for new people to understand. We have also
avoided the Kotlin coroutine API (the default threading model for Kotlin) and it's concepts to make it easier to
understand (which is a big no no).

## Set up
Install the Idea IDE found at https://www.jetbrains.com/idea/download

This IDE will help you a lot with the project setup and Kotlin syntax and errors.

This project has been tested with Java 11, so I advise you to use the same version :).

Install the following plugins for Idea IDE: Kotlin, Sqldelight

I advise you to try this with a new Kotlin project with gradle just so you have no surprises :)

## Running gradle tasks
Kotlin uses Gradle as the build tool. To see how to efficiently use them to run or debug in the IDE read:
* https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#gradle_tasks
* https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#debug_gradle

TL;DR;
* Double press shift to bring up dialog, then type and select "Execute gradle task"
* After you run a task you will see it in the top and can select it and either run or debug it (check 
    [run-debug.png](run-debug.png))

You can also run it from the cmd in the root folder using the gradle wrapper: `./gradlew <task>`

Because we have subprojects, they also have tasks. You can refer to a subproject with the following task name: `:<folder>:<task>`

### Listing available tasks
You can see the available gradle tasks by running the `tasks` task: `./gradlew tasks`.

If you want to see the tasks for a subproject `:<folder>:tasks`: `./gradlew :server:tasks`

## Java to Kotlin quick intro
Kotlin is a modern language developed by Jetbrains to solve the design flaws of Java. Fortunately it has "borrowed" the
good aspects of Java.

Please read [this Medium article](https://proandroiddev.com/the-kotlin-guide-for-the-busy-java-developer-93dde84a77b7)
that should allow you to quickly start. You can skip the "Infix Functions", "Inline Functions", "When", 
"Operator Overloading", "Nested Classes", "Sealed Classes" . And please focus on the "Lambdas" chapter
since it's a very important part of Kotlin.

As a multithreading paradigm, Kotlin focuses on coroutines. It is not important to understand them or how they work for
this exercise. You can think of them as very lightweight threads. We tried to avoid them to make it easier for you to
understand.

Remember this from the article:
```kotlin
// filter is a method that receives a Lambda as argument
// Classic (Java) way of calling it
people.filter({ p: Person -> p.age < 18 })
// Lambda as last argument can be moved out of parentheses in Kotlin!
people.filter() { p: Person -> p.age < 18}
// Lambda as the only argument? You can remove parentheses!
people.filter { p: Person -> p.age < 18 }
// Inferred parameter type (Kotlin will deduce that p is a Person)
people.filter { p -> p.age < 18 }
// Using the default parameter name. If you don't specify a parameter name it will default to 'it'
people.filter { it.age < 18 }
```
You can choose to play a little with Kotlin before starting. It's a free country (at least at the moment of writing this
README...)

## A helping hand
Don't worry. For each task you are expected to do, you have an already made example (so a lot of copy and paste + adapt
:) ). The example is a basic case though, so in some cases you might need to check the documentation. The IDE will also
help you A LOT.

## Project structure
As you can see, the build system is already in place. This project is made up of multiple subproject, each accomplishing
a different role:
* server -> contains the backend (server) code and the database setup scripts
* api -> contains the API objects the server will respond with on ReST calls (keep in mind that the API objects do not
         necessary match the schema of the database).
* client -> contains a library that abstracts the HTTP calls to the server (not needed for exercise)
* app-android -> the Android app (not needed for exercise)
* app-web -> the Web app (not needed for exercise)

[//]: # (Each subproject has a README and a starting point already written, so it should make things very easy to start with.)

[//]: # (On top of that the server has a test script written, so you can easily test if your backend is working before moving)

[//]: # (on to the client code.)

We will focus only on the [api](api) and [server](api)

## API docs
This section describes the API objects the server will traffic and describe the available endpoints.

### Objects
#### User
* id: Long
* firstName: String
* lastName: String
* admin: Boolean

#### Thread
* id: Long
* title: String
* created: Unix timestamp(Long)
* author: User? (question mark means it can be null, in this case the Thread is created by an anonymous user)
* message: String

#### Reply
A reply can either be to a thread or to another reply. Example:
* T1
  * R1 -> Reply to T1
  * R2 -> Reply to T1
    * R3 -> Reply to R2
* T2
  * R4 -> Reply to T2
    * R5 -> Reply to R4
      * R6 -> Reply to R5
        * R7 -> Reply to R6
        * R8 -> Reply to R6


* id: Long
* created: Unix timestamp(Long)
* author: User? (question mark means it can be null, in this case the Reply is created by an anonymous user)
* message: String
* replies: Int

### Endpoints
Building a ReST interface basically boils down to defining resources and applying actions to them.
* resource identifier -> HTTP path
* resource action -> [HTTP verbs](https://www.restapitutorial.com/lessons/httpmethods.html)
* result of action -> [HTTP response codes](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) + optional response body

Let's take for example the ReST endpoint `https://example.com/jokes`. The path is `/jokes` and it describes the `jokes`
resource.

This section describes our API. The title contains the path verb combination.
The path can also be variable, for example path `/users/:id` refers to the user with id `:id`. In case the ID (`:id`)
of the resource does not exist you should return a 404 (Not found) status code. We will be using Long as `:id`.
In case the received `:id` can not be converted to a Long, we need to send back a 400(Bad request).

#### Users endpoint (already built, take it as an example)

##### `/api/users` GET
Retrieve all the available users.

Response: 200(OK)

Response body: `List<User>`

##### `/api/users` POST
Create a new user.

Request body: `User` without `id` field

Response: 201(Created)

Response Body: `User` the newly created user

##### `/api/users/:id` DELETE
Delete the user with the specified ID.

Response: 204(No Content)

#### Threads endpoint

##### `/api/threads` GET
Retrieve all the existing threads

Response: 200(OK)

Response body: `List<Thread>`

##### `/api/threads` POST
Create a new thread.

Request body: An object containing:
* title: String
* message: String

Response: 201(Created)

Response Body: `Thread` the newly created thread

##### `/api/threads/:id` PUT
Update an existing thread with the given title and message

Request body: An object containing:
* title: String
* message: String

Response: 204(No Content)

Response Body: `Thread` the updated thread

##### `/api/threads/:id` DELETE
Delete an existing thread and all its replies

Response: 204(No Content)

#### Replies endpoint

##### `/api/threads/:threadId/replies` GET
Retrieve all the existing replies for the given thread id

Response: 200(OK)

Response body: `List<Replies>`

##### `/api/threads/:threadId/replies` POST
Create a new reply in the current thread.

Request body: An object containing:
* message: String
 
Response: 201(Created)

Response Body: `Reply` the newly created reply

##### `/api/threads/:threadId/replies/:replyId` POST
Create a new reply in the current thread to the given replyId.

Request body: An object containing:
* message: String

Response: 201(Created)

Response Body: `Reply` the newly created reply

##### `/api/replies/:id` PUT
Update an existing thread

Request body: An object containing:
* message: String
  
Response: 204(No Content)

Response Body: `Thread` the updated thread

##### `/api/replies/:id` DELETE
Delete an existing thread and all its replies

Response: 204(No Content)


## How to proceed
### Database design
Take a moment to think how you will design the database. You are free to do whatever you want with it as long as the
backend serves the correct API.

Keep in mind that you can reply to either a thread or to another reply.

Be careful with scalability, think of what your query does, what to index and your keys.

The tables can have a different shape then the API described in the endpoints (and probably
should).

Keep in mind that after deleting a thread, you have to delete the replies associated with it. Also keep in mind
that after deleting a reply you have to delete all the replies to that reply.

### Coding
**WARNING** *once you open the Google forms link a timer for 4 hours 5 minutes will start*. Be sure you have your
IDE/Java ready (maybe also go to the bathroom and grab a snack and some water :) ). It is advised to submit your
response after the 4 hours expire, so you have enough time to package and submit.

Open the project
Each subproject will have instructions on what needs to be added in the README.md. You should proceed in this order:

1. Create the API objects in the [api project](api).
2. Implement the ReST API in the [server project](server). This should also familiarize you with the Kotlin
    programing language (don't worry it is very similar to Java). Run the tests to be sure
    your server is working OK.

[//]: # (3. Implement the client library found in the [client project]&#40;client&#41;. This will abstract the ReST API and provide)

[//]: # (    and make the API calls easy to use.)

[//]: # (4. Implement the Android blog app in the [app-android project]&#40;app-android&#41;.)

[//]: # (5. Implement the user management web app in the [app-web project]&#40;app-web&#41;.)

[//]: # (## Bonus)

[//]: # (Add authentication, so you can also identify who created a post and admins should be)

[//]: # (the only ones who can create/update/delete users.)

[//]: # ()
[//]: # (Make the UI kick ass :&#41;)

## Submitting
Don't worry if you did not pass all the tests. This is not a trivial task for someone new, w/o Kotlin experience and on
a 4h timer.

For someone with experience it should take him ~1h to pass almost all tests of the tests at a leisurely pace.

Push the code to Github as a **PRIVATE** repository. Get the commit link and put it in the form. If you don't know how
to use git to push stuff to Github please look it up (you can't work in software without this).

Add a screenshot with the passed tests as results.png in the root folder.

### How to get the commit link
1. Go to the commits page (`https://github.com/<owner>/<repo>/commits/main`)
2. Click on `<>` icon in the right (Browse this repository at this point in history)
3. Get the URL



package io.almer.api



