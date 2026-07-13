import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "emy.partners.lawapp.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.foundation.pager)
            implementation(libs.ktor.client.okhttp)
            implementation("androidx.media3:media3-exoplayer:1.4.1")
            implementation("androidx.media3:media3-ui:1.4.1")
            implementation(libs.androidx.media3.exoplayer)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.voyager.navigator)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
           // implementation("io.coil-kt:coil-compose:3.0.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.0")
            implementation("io.coil-kt.coil3:coil-compose:3.0.0")
            implementation("io.github.khubaibkhan4:mediaplayer-kmp:2.0.9")
            implementation("io.github.fletchmckee.liquid:liquid:1.1.1")

        }
        appleMain.dependencies {
            // Ktor client dependency required for iOS
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}