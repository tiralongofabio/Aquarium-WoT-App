plugins {
    alias(libs.plugins.kotlin.jvm)
}


// JVM Toolchain: allinea automaticamente 'compileJava' e 'compileKotlin' a Java 17
kotlin {
    jvmToolchain(17)
}


dependencies {
    implementation(libs.coroutines.core)
}



