pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MediAware"

// App entry point
include(":app")

// Core infrastructure modules
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:designsystem")
include(":core:voice")

// Feature modules
include(":features:auth")
include(":features:home")
include(":features:settings")
include(":features:symptom")
include(":features:report")
include(":features:prescription")
include(":features:chamber")
include(":features:consultation")
include(":features:history")
include(":features:caregiver")