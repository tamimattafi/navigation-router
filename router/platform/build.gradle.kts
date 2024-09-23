plugins {
    alias(libs.plugins.android.library)
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.router.base)
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

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
