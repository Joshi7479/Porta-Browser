plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("com.google.devtools.ksp") version "2.3.9"
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.0")
    }
}


android {
    namespace = "com.example.PortaBrowser"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.PortaBrowser"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.datastore.preferences)
    implementation("com.github.Dhaval2404:ColorPicker:2.3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui:1.12.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.1")

    implementation("org.mozilla.geckoview:geckoview:157.0.20261005135250")

    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
