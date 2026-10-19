buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Must match [versions].kotlin in gradle/libs.versions.toml.
        // Only the compat adapter belongs on the plugin classpath: the impl JAR
        // pulls kotlin-compiler-embeddable, which KGP rejects as a build-classpath artifact.
        val kotlinVersion = "2.3.21"
        classpath("org.jetbrains.kotlin:kotlin-build-tools-api:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-build-tools-compat:$kotlinVersion")
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

subprojects {
    pluginManager.withPlugin("com.android.application") {
        ensureKotlinBuildToolsClasspath()
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        ensureKotlinBuildToolsClasspath()
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
        ensureKotlinBuildToolsClasspath()
    }
}

fun Project.ensureKotlinBuildToolsClasspath() {
    val buildToolsArtifacts = listOf(
        libs.kotlin.build.tools.api,
        libs.kotlin.build.tools.impl,
        libs.kotlin.build.tools.compat,
    )
    configurations.configureEach {
        if (name != "kotlinBuildToolsApiClasspath" && name != "kotlinCompilerClasspath") {
            return@configureEach
        }
        val existing = dependencies.map { "${it.group}:${it.name}" }.toSet()
        buildToolsArtifacts.forEach { artifact ->
            val coordinates = "${artifact.get().module.group}:${artifact.get().module.name}"
            if (coordinates !in existing) {
                project.dependencies.add(name, artifact)
            }
        }
    }
}
