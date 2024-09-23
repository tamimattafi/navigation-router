package com.attafitamim.navigation.multiplatform

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class MultiplatformConventions : Plugin<Project> {
  @ExperimentalWasmDsl
  override fun apply(project: Project) {
    project.plugins.apply {
      apply("org.jetbrains.kotlin.multiplatform")
      apply("com.android.library")
    }

    val extension = project.kotlinExtension as KotlinMultiplatformExtension
    extension.apply {
      applyDefaultHierarchyTemplate()
      jvmToolchain(17)

      jvm("desktop")

      androidTarget {
        compilations.all {
          it.kotlinOptions {
            jvmTarget = "17"
          }
        }

        publishLibraryVariants("release")
      }

      js {
        browser {
          testTask {
            it.useKarma {
              useChromeHeadless()
            }
          }
        }
        compilations.configureEach {
          it.kotlinOptions {
            moduleKind = "umd"
          }
        }
      }

      wasmJs {
        binaries.executable()
      }

      iosX64()
      iosArm64()
      iosSimulatorArm64()
    }

    val androidExtension = project.extensions.getByName("android") as LibraryExtension
    androidExtension.apply {
      namespace = "com.attafitamim.navigation.${project.name}"
      compileSdk = 34

      defaultConfig {
        minSdk = 16
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
  }
}
