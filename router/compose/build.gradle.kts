@file:Suppress("OPT_IN_USAGE")

import GradleUtils.correctArtifactId

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.convention.plugin.get().pluginId)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(17)
    jvm("desktop")

    js {
        browser()
    }
    wasmJs {
        binaries.executable()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        publishLibraryVariants("release")
        this.mavenPublication {
            correctArtifactId(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Local
                api(projects.router.core)
                api(projects.router.base)

                // Compose
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material)
                implementation(compose.material3)
            }
        }

        val androidMain by getting {
            dependencies {
                // Compose
                implementation(libs.compose.activity)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

android {
    namespace = "com.attafitamim.navigation.router.compose"
    compileSdk = 34
    defaultConfig {
        minSdk = 16
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}