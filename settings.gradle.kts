// Top-level build file where you can add configuration options common to all sub-projects/modules.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.5.2" // Use the latest version for your project
        id("com.android.library") version "8.5.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.10" apply false
        id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NotesApp"
include(":app")

