plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.mediaware.core.database"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Core model domain classes
    implementation(project(":core:model"))

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room Persistence Library + KSP annotation processor
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SQLCipher AES-256 encryption for Room
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite)

    // Coroutines for Flow-based DAO queries
    implementation(libs.kotlinx.coroutines.android)

    // Kotlinx Serialization for JSON TypeConverters
    implementation(libs.kotlinx.serialization.json)
}
