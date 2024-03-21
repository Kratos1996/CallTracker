@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}
hilt {
    enableAggregatingTask = true
    enableTransformForLocalTests = true
    enableExperimentalClasspathAggregation = true
}
android {
    namespace = "com.ishant.calltracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ishant.calltracker"
        minSdk = 25
        targetSdk = 34
        versionCode = 10
        versionName = "1.1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.dexter)
    implementation (libs.libphonenumber)
    //Retrofit
    implementation (libs.gson)
    implementation (libs.retrofit2.kotlin.coroutines.adapter)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.library)
    releaseImplementation(libs.library.no.op)
    //Dagger - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.kotlinx.metadata.jvm)

    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
// When using Kotlin.
    kapt(libs.hilt.compiler)
// WorkManager with Coroutines
    implementation(libs.androidx.work.runtime.ktx)
    //OKHTTP
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
// Retrofit)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // Room
    implementation (libs.androidx.room.runtime)
    implementation (libs.room.ktx )
    kapt (libs.androidx.room.compiler)
}