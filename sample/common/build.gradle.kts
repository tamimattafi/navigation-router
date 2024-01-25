plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.router.base)
            }
        }
    }
}