plugins {
    id("com.android.application")
}

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
        versionCode = 3
        versionName = "1.3"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVer)
        targetCompatibility = JavaVersion.toVersion(javaVer)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
