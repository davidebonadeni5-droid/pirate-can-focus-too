import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    // L'app iPhone : le code Kotlin est compilé en framework, appelé depuis iosApp/ (Xcode).
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "PirateShared"
            isStatic = true
        }
    }

    // Version ordinateur : pratique pour tester vite sur le PC (./gradlew :app:run).
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "dev.mathieuburnat.piratefocus"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.mathieuburnat.piratefocus"
        minSdk = 26
        targetSdk = 35
        // La CI fournit un numéro qui augmente à chaque version : Android installe alors la mise à jour
        // par-dessus l'ancienne (sans désinstaller, les doublons restent).
        versionCode = (System.getenv("VERSION_CODE") ?: "2").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "0.2.0"
    }

    // Toujours la même clé de signature : sinon Android refuse la mise à jour et il faut réinstaller.
    val keystorePath = System.getenv("PIRATE_KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("PIRATE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("PIRATE_KEY_ALIAS")
                keyPassword = System.getenv("PIRATE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "dev.mathieuburnat.piratefocus.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "PirateFocus"
            // Windows exige MAJEUR.MINEUR.BUILD : la CI fournit un BUILD qui augmente à chaque version.
            packageVersion = System.getenv("DESKTOP_VERSION") ?: "1.0.0"
            description = "Un pirate aussi peut se concentrer"
            vendor = "Pirate Can Focus Too"
            // Sauvegarde (java.util.prefs) et vérification des mises à jour (java.net.http) :
            // à embarquer dans le Java fourni avec l'appli.
            modules("java.prefs", "java.net.http")
            windows {
                // Toujours le même identifiant : une nouvelle version remplace l'ancienne, sans désinstaller.
                upgradeUuid = "6f1c2b7e-3d4a-4c8e-9b2f-5a7d1e0c9f31"
                menuGroup = "Pirate Focus"
                shortcut = true
                menu = true
                perUserInstall = true
                dirChooser = true
            }
        }
    }
}
