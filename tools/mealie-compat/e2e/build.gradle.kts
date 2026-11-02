plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    application
}
kotlin { jvmToolchain(21) }
// The versions below are the ones in gradle/libs.versions.toml
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.ktor:ktor-client-core:2.3.13")
    implementation("io.ktor:ktor-client-okhttp:2.3.13")
    implementation("io.ktor:ktor-client-auth:2.3.13")
    implementation("io.ktor:ktor-client-encoding:2.3.13")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.13")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.13")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.1"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("javax.inject:javax.inject:1")
    runtimeOnly("org.slf4j:slf4j-nop:2.0.13")
}
application { mainClass.set((findProperty("main") as String?) ?: "gq.kirmanak.mealient.datasource.impl.MainKt") }
