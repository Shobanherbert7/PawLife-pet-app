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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PawLife"
include(":app")

// Redirect build outputs to the system temp directory to completely bypass OneDrive file locking
gradle.beforeProject {
    layout.buildDirectory.set(File(System.getProperty("java.io.tmpdir"), "PawLife_Build/${project.name}"))
}
