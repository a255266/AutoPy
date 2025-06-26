pluginManagement {
    repositories {
        maven { url=uri ("https://maven.aliyun.com/repository/releases")}
        maven { url=uri ("https://maven.aliyun.com/repository/google")}
        maven { url=uri ("https://maven.aliyun.com/repository/central")}
        maven { url=uri ("https://maven.aliyun.com/repository/gradle-plugin")}
        maven { url=uri ("https://maven.aliyun.com/repository/public")}
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url=uri ("https://chaquo.com/maven")}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url=uri ("https://maven.aliyun.com/nexus/content/repositories/google") }
        maven { url=uri ("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url=uri ("https://maven.aliyun.com/nexus/content/repositories/jcenter")}
        google()
        mavenCentral()
        maven { url=uri ("https://chaquo.com/maven")}
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "AutoPy"
include(":app")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
 