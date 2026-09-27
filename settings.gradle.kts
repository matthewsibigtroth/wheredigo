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
        // Mapbox Downloads Maven Repository (Requires secret token with DOWNLOADS:READ scope)
        maven {
            url = java.net.URI("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below. It should always be "mapbox" (not your personal account name).
                username = "mapbox"
                // Password retrieved from gradle.properties or local.properties (MAPBOX_DOWNLOADS_TOKEN)
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                    .orNull
                    ?: java.util.Properties().apply {
                        val localPropsFile = java.io.File(rootDir, "local.properties")
                        if (localPropsFile.exists()) {
                            load(localPropsFile.inputStream())
                        }
                    }.getProperty("MAPBOX_DOWNLOADS_TOKEN")
                    ?: ""
            }
        }
    }
}

rootProject.name = "Whered I Go"
include(":app")
