
group = "in.francl"
version = "0.0.1"

application {
    mainClass.set("in.francl.cam.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.+"
    kotlin("kapt") version "2.+"
    kotlin("plugin.serialization") version "2.+"
    id("io.ktor.plugin") version "2.+"
    id("org.jetbrains.dokka") version "1.+"
    id("jacoco")
}

dependencies {

    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")

    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-double-receive")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-network-tls-certificates")
    implementation("io.ktor:ktor-serialization-gson")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-jetty")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-auth")

    implementation("org.camunda.bpm:camunda-external-task-client:7.+")

    implementation(platform("io.insert-koin:koin-bom:3.+"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-core-coroutines")
    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    implementation(platform("io.arrow-kt:arrow-stack:1.+"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-resilience")
    implementation("io.arrow-kt:arrow-optics")
    implementation("io.arrow-kt:arrow-atomic")
    implementation("io.arrow-kt:arrow-collectors")
    implementation("io.arrow-kt:arrow-eval")
    implementation("io.arrow-kt:arrow-cache4k")
    implementation("io.arrow-kt:arrow-core-serialization")

    implementation(platform("io.micrometer:micrometer-bom:latest.release"))
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:context-propagation")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation(platform("ch.qos.logback:logback-parent:1.+"))
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-core")

    implementation("net.logstash.logback:logstash-logback-encoder:7.+")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.+")
    implementation("com.sun.xml.bind:jaxb-impl:3.+")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.insert-koin:koin-test")
    testImplementation("io.insert-koin:koin-test-junit5")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Garante que os testes sejam executados antes do relatório

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(file("build/reports/jacoco"))
    }
}

tasks.test {
    filter {
        includeTestsMatching("in.francl.cam.unit.*")
    }
    useJUnitPlatform()
    reports {
        junitXml.configure(
            closureOf<Any> {
                isEnabled = true
            }
        )
    }
    finalizedBy(tasks.jacocoTestReport) // Gera o relatório após os testes
}
