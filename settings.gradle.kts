pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/amper/amper")
    }
}

plugins {
    // apply the plugin:
    id("org.jetbrains.amper.settings.plugin").version("0.1.4")
}

rootProject.name = "magick-kt-tooling"

// apply the plugin:
plugins.apply("org.jetbrains.amper.settings.plugin")
