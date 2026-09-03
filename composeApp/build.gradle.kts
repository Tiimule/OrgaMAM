import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            
            implementation(libs.kotlinx.datetime)
            
            // Firebase KMP
            implementation(libs.firebase.common)
            implementation(libs.firebase.auth.kmp)
            implementation(libs.firebase.firestore.kmp)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        iosMain.dependencies {
        }
    }
}

android {
    namespace = "com.example.orgamam"
    compileSdk = 35

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = localProperties.getProperty("signing.storeFile")?.let { file(it) }
            storePassword = localProperties.getProperty("signing.storePassword")
            keyAlias = localProperties.getProperty("signing.keyAlias")
            keyPassword = localProperties.getProperty("signing.keyPassword")
        }
    }

    defaultConfig {
        applicationId = "com.example.orgamam"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.6"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
