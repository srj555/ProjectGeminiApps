plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.projectduoplay.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "app"

    productFlavors {
        create("duoplay") {
            dimension = "app"
            applicationId = "com.duoplay.app"
            versionNameSuffix = "-duoplay"

            // Flavor-specific build config
            buildConfigField("String", "APP_NAME", "\"DuoPlay\"")
            buildConfigField("String", "BASE_URL", "\"https://duoplay-589720827506.us-west1.run.app\"")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("boolean", "ENABLE_PUSH_NOTIFICATIONS", "true")

            // Resource values
            resValue("string", "app_name", "DuoPlay")
            resValue("string", "base_url", "https://duoplay-589720827506.us-west1.run.app")
        }

        // Template for creating new flavors
        // Uncomment and modify for new apps
        /*
        create("myapp") {
            dimension = "app"
            applicationId = "com.myapp.app"
            versionNameSuffix = "-myapp"

            buildConfigField("String", "APP_NAME", "\"MyApp\"")
            buildConfigField("String", "BASE_URL", "\"https://myapp.example.com\"")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("boolean", "ENABLE_PUSH_NOTIFICATIONS", "true")

            resValue("string", "app_name", "MyApp")
            resValue("string", "base_url", "https://myapp.example.com")
        }
        */
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(projects.feature.webview)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    debugImplementation(libs.compose.ui.tooling)
}
