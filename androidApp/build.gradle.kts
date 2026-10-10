import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

val appProfileId = providers.gradleProperty("appProfile").orNull
    ?: throw GradleException(
        "Missing -PappProfile. Run ./wrapper build --profile <id> --platform android ..."
    )
if (!appProfileId.matches(Regex("^[a-z][a-z0-9_]*$"))) {
    throw GradleException("Invalid appProfile: $appProfileId")
}
val generatedProfileDirectory = rootProject.file("config/generated/profiles/$appProfileId")
val profilePropertiesFile = generatedProfileDirectory.resolve("android.properties")
if (!profilePropertiesFile.isFile) {
    throw GradleException(
        "Generated profile not found for $appProfileId. " +
            "Run ./wrapper generate --profile $appProfileId --platform android"
    )
}
val profileProperties = Properties().apply {
    profilePropertiesFile.inputStream().use(::load)
}
fun profileValue(key: String): String = profileProperties.getProperty(key)
    ?: throw GradleException("Generated profile $appProfileId is missing $key")
fun quotedBuildConfig(value: String): String = buildString {
    append('"')
    value.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(character)
        }
    }
    append('"')
}

layout.buildDirectory.set(rootProject.layout.projectDirectory.dir(".build/android/$appProfileId/androidApp"))

android {
    namespace = "com.projectduoplay.android"
    compileSdk = 35

    defaultConfig {
        applicationId = profileValue("android.applicationId")
        minSdk = 24
        targetSdk = 35
        versionCode = profileValue("app.versionCode").toInt()
        versionName = profileValue("app.versionName")
        manifestPlaceholders["appHost"] = profileValue("android.deepLinkHost")

        buildConfigField("String", "APP_NAME", quotedBuildConfig(profileValue("app.name")))
        buildConfigField("String", "APP_SUBTITLE", quotedBuildConfig(profileValue("app.subtitle")))
        buildConfigField("String", "BASE_URL", quotedBuildConfig(profileValue("app.startUrl")))
        buildConfigField("String", "ALLOWED_ORIGINS", quotedBuildConfig(profileValue("app.allowedOrigins")))
        buildConfigField(
            "boolean",
            "ENABLE_JAVASCRIPT_BRIDGE",
            profileValue("app.enableJavaScriptBridge")
        )
        buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        buildConfigField("boolean", "ENABLE_PUSH_NOTIFICATIONS", "false")
        resValue("string", "app_name", profileValue("app.name"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Load keystore properties for release signing
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    sourceSets.getByName("main").res.srcDir(generatedProfileDirectory.resolve("android/res"))

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
