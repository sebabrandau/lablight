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
        // MPAndroidChart (librería de gráficos) se publica en JitPack
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "LabLight"
include(":app")
