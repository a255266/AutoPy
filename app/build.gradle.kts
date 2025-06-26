plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.chaquo.python")
    kotlin("kapt")
    alias(libs.plugins.hilt.plugin)
}

android {
    namespace = "com.python"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.python"
        minSdk = 31
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", )
        }
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
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }
    kotlinOptions {
        jvmTarget = "11"
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
    kapt("com.google.dagger:hilt-compiler:2.56.2")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

//    implementation("com.google.accompanist:accompanist-pager:0.35.0-alpha") // 版本请与 Compose 对齐
//    implementation("com.google.accompanist:accompanist-pager-indicators:0.35.0-alpha")
    implementation("com.github.bitfireAT:dav4jvm:2.2.1") {
        exclude(group = "org.ogce", module = "xpp3")
        exclude(group = "xmlpull", module = "xmlpull")
        exclude(group = "org.xmlpull", module = "xmlpull")
    }

}