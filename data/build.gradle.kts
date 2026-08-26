plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.unibo.android.automazione.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":domain"))

    // Hilt per Dependency Injection
    implementation(libs.hilt.android)
    "ksp"(libs.hilt.compiler)

    // Room DB (Single Source of Truth)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    "ksp"(libs.room.compiler)

    // Networking (Retrofit per chiamate HTTP su Emulatore)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // ML Kit Scanner (Lettura QR Code)
    implementation(libs.mlkit.barcode)

    // WorkManager (Background polling e notifiche)
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.coroutines.android)
}


