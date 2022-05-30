plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

ext {
    set("PUBLISH_ARTIFACT_ID", "base")
}

apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(project(":router:core"))
}
