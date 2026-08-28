pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OmniGFX"

include(":app")
include(":core:core-shizuku")
include(":core:core-engine")
include(":core:core-database")
include(":core:core-ui")
include(":feature:feature-home")
include(":feature:feature-wizard")
include(":feature:feature-config")
