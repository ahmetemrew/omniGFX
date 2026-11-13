import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.basitce.gfx"
    compileSdk = 37

    defaultConfig {
        applicationId = providers.gradleProperty("omnigfx.applicationId").getOrElse("com.basitce.gfx")
        minSdk = 26
        targetSdk = 37
        versionCode = 21
        versionName = "2.0.0-PRO"
    }

    lint {
        // Shared project-wide lint config (intentional dependency pins, etc.).
        lintConfig = rootProject.file("lint.xml")
        // Incomplete legacy locale resources use the default text until translated.
        warning += "MissingTranslation"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        val keyStore = providers.environmentVariable("OMNIGFX_KEYSTORE_PATH").orNull
        if (!keyStore.isNullOrBlank()) {
            create("production") {
                storeFile = file(keyStore)
                storePassword = providers.environmentVariable("OMNIGFX_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("OMNIGFX_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("OMNIGFX_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("production")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.14.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")

    // Feature Modules
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-wizard"))

    // Core Modules
    implementation(project(":core:core-database"))
    implementation(project(":core:core-engine"))
    implementation(project(":core:core-ui"))

    // Shizuku API
    implementation("dev.rikka.shizuku:api:12.1.0")
    implementation("dev.rikka.shizuku:provider:12.1.0")

    // Tests
    testImplementation("junit:junit:4.13.2")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
}