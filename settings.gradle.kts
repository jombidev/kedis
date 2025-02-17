pluginManagement {
    val dokkaVersion: String by settings
    val kotlinVersion: String by settings
    val koverVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
        id("org.jetbrains.kotlinx.kover") version koverVersion apply false
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    if (System.getenv("IS_CI") == "yes") {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    } else {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    }
    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io/")
        mavenLocal()
    }
}

rootProject.name = "kedis"

include(":kedis")

if (System.getenv("IS_CI") != "yes") {
    include(":example")
}
