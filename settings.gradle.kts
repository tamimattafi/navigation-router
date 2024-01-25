enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "navigation-router"
include(":router:core")
include(":router:base")
include(":router:platform")
include(":router:compose")
include(":sample:android")
include(":sample:common")
include(":sample:composeApp")

// Publish
includeBuild("convention-plugins")
