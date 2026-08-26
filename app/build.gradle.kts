plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.unibo.android.automazione"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unibo.android.automazione"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions.add("environment")
    productFlavors {
        create("emulator") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        create("device") {
            dimension = "environment"
            buildConfigField("String", "BLE_SERVICE_UUID", "\"12345678-1234-5678-1234-56789abcdef0\"")
        }
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
    implementation(project(":data"))
    implementation(project(":uicompose"))

    // Hilt
    implementation(libs.hilt.android)
    "ksp"(libs.hilt.compiler)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}


