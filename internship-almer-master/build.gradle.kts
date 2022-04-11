

plugins {
    id("com.android.library") apply false
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    kotlin("jvm") apply false
    kotlin("js") apply false
    id("com.squareup.sqldelight") apply false
    id("org.jetbrains.compose") apply false
}
buildscript {
    val compose_version by extra("1.0.0")
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
    }
}
