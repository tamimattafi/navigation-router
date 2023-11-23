import GradleUtils.correctArtifactId

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id(libs.plugins.convention.plugin.get().pluginId)
}

kotlin {
    targetHierarchy.default()
    jvm()

    js {
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
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
                api(project(":router:base"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.10.0")
            }
        }
    }
}

android {
    namespace = "com.attafitamim.navigation.router.platform"
    compileSdk = 34
    defaultConfig {
        minSdk = 16
    }
}