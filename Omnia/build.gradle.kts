// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
    }
    dependencies {
        val nav_version = "2.9.8"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.4.10" apply false
}