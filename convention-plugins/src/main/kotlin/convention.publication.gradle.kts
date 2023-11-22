import GradleUtils.correctArtifactId
import java.util.Properties

plugins {
    `maven-publish`
    signing
}


// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.key"] = null

group = "com.attafitamim.navigation"
version = "2.0.0"

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile: File = project.rootProject.file("sign.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.key"] = System.getenv("SIGNING_KEY")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// TODO refactor not to add in every gradle module android specs
//fun addAndroidProperty(path: String) {
//    val file = Path.of("$path/build.gradle.kts").toFile()
//    val lines = file.readLines().toMutableList()
//    val kotlinLine = lines.indexOf("kotlin {").takeIf { it != -1 } ?: return
//
//    val release = "androidTarget { publishLibraryVariants(\"release\") }"
//
//    lines.add(kotlinLine + 1, release)
//
//    file.writeText(lines.joinToString(separator = "\n"))
//}
//allprojects.forEach { project ->
//    addAndroidProperty(project.projectDir.absolutePath)
//}

publishing {
    // Configure sonatype maven repository
    repositories {
        mavenLocal()
        mavenCentral()
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar)

        // Provide artifacts information requited by Maven publish
        pom {
            name.set("Navigation Router")
            description.set("Navigation libraries for Android and JVM")
            url.set("https://github.com/tamimattafi/navigation-router")

            licenses {
                license {
                    name.set("Library Licence")
                    url.set("https://github.com/tamimattafi/navigation-router/blob/main/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("attafitamim")
                    name.set("Tamim Attafi")
                    email.set("attafitamim@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:github.com/tamimattafi/navigation-router.git")
                developerConnection.set("scm:git:ssh://github.com/tamimattafi/navigation-router.git")
                url.set("https://github.com/tamimattafi/navigation-router/tree/main")
            }
        }
    }
}
/*
signing {
    useInMemoryPgpKeys(
        rootProject.ext["signing.keyId"],
        rootProject.ext["signing.key"],
        rootProject.ext["signing.password"],
    )
    sign publishing.publications
}*/

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            correctArtifactId(this)
        }
    }
}
