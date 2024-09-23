plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins.create("multiplatform") {
        id = "com.attafitamim.navigation.multiplatform"
        implementationClass = "com.attafitamim.navigation.multiplatform.MultiplatformConventions"
    }
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.android.build.tools)
}
