plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val releaseTag = System.getenv("GITHUB_REF_NAME")
val versionNameValue = releaseTag?.removePrefix("v")?.takeIf { it.isNotBlank() } ?: "1.0.0"
val versionCodeValue = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.coerceAtLeast(1) ?: 1

val keystorePath = providers.gradleProperty("keystorePath").orNull
val keystorePassword = providers.gradleProperty("keystorePassword").orNull
val keyAlias = providers.gradleProperty("keyAlias").orNull
val keyPassword = providers.gradleProperty("keyPassword").orNull

val hasReleaseSigning = listOf(
    keystorePath,
    keystorePassword,
    keyAlias,
    keyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.kxnst.bugsgame"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kxnst.bugsgame"
        minSdk = 28
        targetSdk = 36
        versionCode = versionCodeValue
        versionName = versionNameValue
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                val releaseKeystorePath = requireNotNull(keystorePath)
                val releaseKeystorePassword = requireNotNull(keystorePassword)
                val releaseKeyAlias = requireNotNull(keyAlias)
                val releaseKeyPassword = requireNotNull(keyPassword)

                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                this.keyAlias = releaseKeyAlias
                this.keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        getByName("release") {
            isMinifyEnabled = false

            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
