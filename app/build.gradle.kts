plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.chaquo.python")
//    id("kotlin-kapt")
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.python"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.python"
        minSdk = 31
        targetSdk = 35
        versionCode = 9
        versionName = "1.0.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // 启用 R8 / ProGuard
            isShrinkResources = true // 启用资源压缩，移除未使用资源
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
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    flavorDimensions += "pyVersion"
    productFlavors {
        create("py311") { dimension = "pyVersion" }
    }
}

chaquopy {
    defaultConfig {
        pip {
            install("requests")
        }
    }
    productFlavors {
        getByName("py311") { version = "3.11" }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp("com.google.dagger:hilt-compiler:2.56.2")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.github.bitfireAT:dav4jvm:2.2.1") {
        exclude(group = "org.ogce", module = "xpp3")
        exclude(group = "xmlpull", module = "xmlpull")
        exclude(group = "org.xmlpull", module = "xmlpull")
    }

}