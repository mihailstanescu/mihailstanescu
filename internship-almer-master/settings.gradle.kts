rootProject.name = "internship"


pluginManagement {
    val kotlin_version: String by settings
    val compose_version: String by settings

    repositories {
//        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        id("com.android.library") version "7.0.3" apply false
        kotlin("multiplatform") version kotlin_version apply false
        kotlin("plugin.serialization") version kotlin_version apply false
        kotlin("jvm") version kotlin_version apply false
        kotlin("js") version kotlin_version apply false
        id("com.squareup.sqldelight") version "1.5.3" apply false
        id("org.jetbrains.compose") version compose_version apply false
    }
}

include("server")
include("api")

// you can include the files from here :)
//include("client")
//include("app-web")
//include("app-android")
