plugins {
    id(libs.plugins.convention.multiplatform.get().pluginId)
    id(libs.plugins.convention.publish.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.router.core)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
