import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

// Set when running on a build server.
val isCi = System.getenv("CI") == "true"

/**
 * Version from gradle.properties: versionMajor.versionMinor[.versionPatch[.versionBuild]].
 * The patch is only shown when it isn't 0; the build only when it isn't 0, together with the patch.
 */
fun buildVersionName(): String {
    val major = property("versionMajor").toString()
    val minor = property("versionMinor").toString()
    val patch = property("versionPatch").toString()
    val build = property("versionBuild").toString()
    var version = "$major.$minor"
    if (build != "0") {
        version += ".$patch.$build"
    } else if (patch != "0") {
        version += ".$patch"
    }
    println("Version : $version")
    return version
}

/** major * 1000000 + minor * 10000 + patch * 100 + build, plus the number after the dot of versionSuffix. */
fun buildVersionCode(): Int {
    val major = property("versionMajor").toString().toInt()
    val minor = property("versionMinor").toString().toInt()
    val patch = property("versionPatch").toString().toInt()
    val build = property("versionBuild").toString().toInt()
    val suffix = findProperty("versionSuffix")?.toString().orEmpty()
    val suffixParts = suffix.split(".").filter { it.isNotEmpty() }
    val suffixInt = if (suffixParts.size > 1) suffixParts[1].toInt() else 0
    return major * 1_000_000 + minor * 10_000 + patch * 100 + build + suffixInt
}

android {
    namespace = "dynoapps.exchange_rates"
    compileSdk = libs.versions.compileSdk.get().toInt()

    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file("signing/debug.keystore")
            storePassword = "android"
        }
        create("release") {
            if (isCi) {
                // CI writes release.jks from a secret before building. Without it the
                // production bundle is built unsigned instead of failing the build.
                val ciKeystore = rootProject.file("release.jks")
                if (ciKeystore.exists()) {
                    storeFile = ciKeystore
                    storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                    keyAlias = System.getenv("RELEASE_ALIAS")
                    keyPassword = System.getenv("RELEASE_PASSWORD")
                } else {
                    println("release.jks is missing; production build will be unsigned")
                }
            } else {
                val signingPropsFile = rootProject.file("signing/release.signing.properties")
                if (signingPropsFile.canRead()) {
                    val props = Properties().apply { signingPropsFile.inputStream().use { load(it) } }
                    val keys = listOf("STORE_FILE", "STORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
                    if (keys.all { props.containsKey(it) }) {
                        storeFile = file(props.getProperty("STORE_FILE"))
                        storePassword = props.getProperty("STORE_PASSWORD")
                        keyAlias = props.getProperty("KEY_ALIAS")
                        keyPassword = props.getProperty("KEY_PASSWORD")
                    } else {
                        println("release.signing.properties file found but some entries are missing")
                    }
                } else {
                    println("release.signing.properties file is missing")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES", "META-INF/LICENSE", "META-INF/LICENSE.txt",
                "META-INF/license.txt", "META-INF/NOTICE", "META-INF/NOTICE.txt",
                "META-INF/notice.txt", "META-INF/ASL2.0", "META-INF/*.version",
                "META-INF/proguard/*", "/*.properties", "META-INF/*.properties",
            )
        }
    }

    defaultConfig {
        applicationId = "dynoapps.exchange_rates"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = buildVersionCode()
        versionName = buildVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = " Debug"
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
        create("production") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-android-java.pro")
            val release = signingConfigs.getByName("release")
            signingConfig = if (release.storeFile != null) release else null
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }

    lint {
        checkDependencies = true
        checkReleaseBuilds = false
        disable += listOf("StringFormatMatches", "Instantiatable")
        ignoreTestSources = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

kapt {
    correctErrorTypes = true
}

base {
    archivesName.set(android.defaultConfig.versionName)
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.adapter.rxjava3)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    implementation(libs.jsoup)
    implementation(libs.mpandroidchart)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.rxjava3)
    kapt(libs.androidx.room.compiler)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
