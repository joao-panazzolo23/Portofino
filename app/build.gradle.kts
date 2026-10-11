plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

android {
    namespace = "com.portofino.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.portofino.launcher"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}