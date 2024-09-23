plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
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
