plugins {
    alias(libs.plugins.android.application)

    // for firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "androidev.thegreenroom"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "androidev.thegreenroom"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // for DataStore preferences
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.datastore:datastore-preferences-core:1.2.1")

    // for firebase
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))

    // for firestore
    implementation("com.google.firebase:firebase-firestore")

    // for cloud storage
    implementation("com.google.firebase:firebase-storage")

    // for glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // to allow api
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}