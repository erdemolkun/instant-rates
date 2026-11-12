// Top-level build file. Module configuration lives in app/build.gradle.kts,
// dependency versions in gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.versions)
}
