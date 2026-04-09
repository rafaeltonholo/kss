pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    }
}

rootProject.name = "kss-publishing"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
    }
}

include(":core")
include(":lexer")
include(":parser")
include(":bom")
