plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

group = "maxamiri.github.io"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

// ktlint plugin configuration
ktlint {
    version.set("0.49.1")
    verbose.set(true)
    android.set(false)
    ignoreFailures.set(false)
}
