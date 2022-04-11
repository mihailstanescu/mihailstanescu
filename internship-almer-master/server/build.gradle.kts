val ktor_version: String by project
val kotlin_version: String by project
val lighthouse_logging: String by project

plugins {
    application
    kotlin("jvm")
    id("com.squareup.sqldelight")
}

group = "io.almer"
version = "1.0.0"
application {
    mainClass.set("io.almer.Application")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("org.lighthousegames:logging:$lighthouse_logging")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("com.squareup.sqldelight:sqlite-driver:1.5.3")

    implementation(project(":api"))

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation(kotlin("test"))


}

sqldelight {
    database("BlogDB") {
        packageName = "io.almer.db"
    }
}

tasks.test {
    useJUnitPlatform()
}

//tasks.getByName<Jar>("jar") {
//    // Get the resources
//    val browserDistributionTask = tasks.getByPath(":gaia-web-app:browserDistribution")
//    dependsOn(browserDistributionTask)
//    from(browserDistributionTask.outputs) {
//        into("static")
//    }
//}