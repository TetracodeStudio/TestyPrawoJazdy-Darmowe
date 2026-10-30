import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val appVersionName = "1.0.1"
val appVersionCode = 2

abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>
    @get:Input
    abstract val versionCode: Property<Int>
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val file = outputDir.get().file("com/testy/BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.testy

object BuildConfig {
    const val VERSION_NAME: String = "${versionName.get()}"
    const val VERSION_CODE: Int = ${versionCode.get()}
    const val PACKAGE_VERSION: String = "${versionName.get()}"
}
            """.trimIndent()
        )
    }
}

val generateBuildConfig = tasks.register<GenerateBuildConfigTask>("generateBuildConfig") {
    versionName.set(appVersionName)
    versionCode.set(appVersionCode)
    outputDir.set(layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin"))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        mainRun {
            mainClass.set("com.testy.MainKt")
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateBuildConfig)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("uk.co.caprica:vlcj:4.12.1")
                implementation("uk.co.caprica:vlcj-natives:4.12.0")
            }
        }
    }
}

android {
    namespace = "com.testy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.testy"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/main/res")

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = false
    }
}

compose.desktop {
    application {
        mainClass = "com.testy.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            packageName = "PrawoJazdyTesty"
            packageVersion = appVersionName
        }
    }
}
