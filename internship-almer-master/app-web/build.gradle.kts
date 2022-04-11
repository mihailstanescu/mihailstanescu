val ktor_version: String by project
val lighthouse_logging: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "io.almer"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    js(IR) {
        browser {
            runTask {
                devServer = devServer?.copy(
                    port = 3200,
                    open = true,
                    proxy = mutableMapOf(
                        "/api" to "localhost:3000",
                    )
                )
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation(project(":client"))
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation("org.lighthousegames:logging:$lighthouse_logging")

            }
        }
    }
}
