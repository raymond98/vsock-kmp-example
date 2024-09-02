plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "machankura.vsock.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    // JVM target
    jvm {
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                implementation(files("libs/vsockk-jvm-1.0-SNAPSHOT.jar"))
            }
        }
        commonMain {
            dependencies {
                implementation(files("libs/vsock-jvm-1.0.0.jar"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
    }
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Run the VSockClient"
    classpath = kotlin.targets["jvm"].compilations["main"].output.allOutputs + configurations["jvmRuntimeClasspath"]
    mainClass.set("machankura.vsock.example.ClientKt")
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Run the VSockServer"
    classpath = kotlin.targets["jvm"].compilations["main"].output.allOutputs + configurations["jvmRuntimeClasspath"]
    mainClass.set("machankura.vsock.example.ServerKt")
}
