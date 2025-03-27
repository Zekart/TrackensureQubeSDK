plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlinx-serialization")
}

android {
    namespace = "com.zekart.trackensurequbesdk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zekart.trackensurequbesdk"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Nordic
    implementation ("no.nordicsemi.android:log:2.3.0")
    implementation ("no.nordicsemi.android:ble:2.7.3")
    implementation ("no.nordicsemi.android:ble-common:2.7.3")
    implementation ("no.nordicsemi.android.support.v18:scanner:1.6.0")

    implementation ("joda-time:joda-time:2.9.9")
}