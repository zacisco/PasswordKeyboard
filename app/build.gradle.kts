plugins {
    id("com.android.application")
}

val appName = "PasswordKeyboard"
val javaVer = 11

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVer))
    }
}

android {
    namespace = "com.zac.pswdKb"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zac.passwordkeyboard"
        minSdk = 21
        targetSdk = 36
        versionCode = 5
        versionName = "1.3.2"
        base.archivesName = "${appName}_${versionName}"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVer)
        targetCompatibility = JavaVersion.toVersion(javaVer)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    testImplementation("junit:junit:4.13.2")
}
