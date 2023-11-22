plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id(libs.plugins.convention.plugin.get().pluginId)
}

kotlin {
    jvm()
    js {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting
    }
}