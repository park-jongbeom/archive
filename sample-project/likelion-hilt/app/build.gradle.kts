import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.likelion.liontalk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.likelion.liontalk"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //jetpack compose
    implementation(libs.compose.nav)
    implementation(libs.coroutines.core)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    implementation(libs.lifecycle.viewmodel.ktx)

    //db
    implementation(libs.room.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.runtime.livedata)

    //기타
    implementation(libs.coil.compose)

    implementation(libs.gson)

    kapt(libs.room.compiler)

    // Firebase
//    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
//    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
//    implementation("com.google.firebase:firebase-analytics:22.1.2")


//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.auth.ktx)
//    implementation(libs.firebase.firestore.ktx)
//    implementation(libs.firebase.functions.ktx)
//    implementation(libs.play.services.auth)
//    implementation(libs.coroutines.play.services)

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

//    implementation("com.google.android.gms:play-services-auth:21.4.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // kakao auth
//    implementation("com.kakao.sdk:v2-user:2.21.6")
    implementation(libs.kakao.auth)
    // naver auth
//    implementation("com.navercorp.nid:oauth:5.10.0")
    implementation(libs.naver.auth)

    // Ktor (used by remote datasources)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.gson)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}