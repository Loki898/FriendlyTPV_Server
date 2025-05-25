
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    // Lector pdf
    implementation("org.apache.pdfbox:pdfbox:2.0.29")
    //QR
    implementation("com.google.zxing:core:3.5.0")
    implementation("com.google.zxing:javase:3.5.0")
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")


// MySQL o MariaDB driver
    implementation("mysql:mysql-connector-java:8.0.33") // O usa mariadb-java-client si usas MariaDB

// HikariCP para el pool de conexiones (recomendado)
    implementation("com.zaxxer:HikariCP:5.1.0")
}
