import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

dependencies {
    implementation(project(":kin"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("io.netty:netty-all:4.1.43.Final")
    implementation("tv.cntt:netty-router:2.2.0")
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("com.github.docker-java:docker-java:3.1.5")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.29")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
val mainClass = "poky.MainKt"

application {
    // Define the main class for the application.
    mainClassName = mainClass
}

tasks.withType<Jar> {
    archiveFileName.set("poky.jar")
    manifest {
        attributes(mapOf(
                "Main-Class" to mainClass
        ))
    }
}

tasks.register("dockerBuild", Exec::class) {
    dependsOn("shadowJar")
    commandLine = "docker build -t poky .".split(" ")
}
