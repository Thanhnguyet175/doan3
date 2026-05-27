plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.milkteaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.milkteaapp"
        minSdk = 24
        targetSdk = 35
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
    }

    // Fix lỗi sourceSets cho KSP (Hilt code generation)
    sourceSets {
        getByName("debug") {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        getByName("release") {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

dependencies {
    // Jetpack Compose & Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")

    implementation("androidx.compose.material:material-icons-extended:1.6.0") // (phiên bản có thể thay đổi tùy project của bạn)

    // Firebase (Đã sửa: Sử dụng text trực tiếp không thông qua toml để tránh lỗi ép version 22.0.1)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.litert.support.api)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.androidx.compose.foundation.layout)
    ksp(libs.hilt.compiler)

    // Mạng & Tiện ích khác
    implementation(libs.volley)

    // Navigation Compose + Hilt Navigation Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Lifecycle Compose (collectAsStateWithLifecycle)
    implementation(libs.androidx.lifecycle.compose)

    // Coroutines (tasks.await cho Firebase)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}