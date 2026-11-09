plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.volleyball.scoreboard"
    compileSdk = 34

    val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 100

    defaultConfig {
        applicationId = "com.volleyball.scoreboard"
        minSdk = 24
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("scoreboard-signing-key.jks")
            val pass = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() } ?: "volleyball123"
            storePassword = pass
            keyAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() } ?: "scoreboard"
            keyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() } ?: pass
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.webkit:webkit:1.10.0")
}
