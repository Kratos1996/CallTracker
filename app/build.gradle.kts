import java.text.SimpleDateFormat
import java.util.Date
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id ("org.jetbrains.kotlin.plugin.serialization")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}
hilt {
    enableAggregatingTask = true
    enableExperimentalClasspathAggregation = true
}
android {
    namespace = "com.ishant.callsoftware"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ishant.callsoftware"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    applicationVariants.all {
        val variant = this
        val formattedDate = SimpleDateFormat("MMM_yyyy").format(Date())
        this.outputs.all {
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                this.outputFileName =
                    "CallSoftware${variant.buildType.name}_${formattedDate}_v${defaultConfig.versionName}.apk"
            }
        }
    }
    buildTypes {
        getByName("debug") {
            /*  isDebuggable = true
              isCrunchPngs = false
              *//*isMinifyEnabled = true*//*
            *//* isShrinkResources = true*//*
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules-debug.pro"
            )*/
        }
        getByName("release") {

            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.txt"
                )
            )
        }

    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/app_release.kotlin_module"
        }

        sourceSets.getByName("main") {
            java.srcDir("src/main/java")
            java.srcDir("src/main/kotlin")
            java.srcDir("src/main/assets")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        /* freeCompilerArgs = listOf("-Xjvm-default=compatibility")*/
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
        buildConfig = true
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
    androidResources {
        additionalParameters.add("--warn-manifest-validation")
    }

}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    //implementation (libs.core.library.compose)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity)
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
    ksp (libs.androidx.room.compiler)
    implementation (libs.ccp)
    // Compose Libs
    debugImplementation(libs.androidx.compose.composeUiTooling)
    implementation(libs.bundles.org.ishant.compose.libs )
    implementation(libs.lifecycle.viewmodel.compose)
    implementation ("com.github.Kratos1996:corelib:1.1.0@aar")
    implementation(libs.bundles.image.libs)
}