plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlinx.atomicfu") version "0.26.0"
}

group = "com.github.enovtapke"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:lincheck:2.34")
}

tasks.test {
    useJUnitPlatform()
}