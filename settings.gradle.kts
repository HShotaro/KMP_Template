rootProject.name = "KMPTemplate"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":composeApp")
include(":shared")
include(":shared:core")
include(":shared:domain")
include(":shared:data")
include(":shared:network")
include(":shared:testing")
include(":shared:ui-model")
