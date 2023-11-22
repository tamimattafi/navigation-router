pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "navigation-router"
include(":router:core")
include(":router:base")
include(":router:android")
include(":sample:android")
include(":sample:common")

// Publish
includeBuild("convention-plugins")