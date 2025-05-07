plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // safeArgs
    alias(libs.plugins.navigation.safe.args)

    // hilt
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
    packaging {
        resources {
            // build 실패를 일으키는 중복 메타-데이터 파일 제거
            excludes += setOf(
                "/META-INF/INDEX.LIST",
                "/META-INF/DEPENDENCIES"
            )
        }
    }
}

dependencies {
    implementation(libs.fitrus.est.device)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // retrofit
    // https://github.com/square/retrofit
    implementation(libs.retrofit)
    // https://github.com/square/okhttp
    implementation(libs.okhttp)
    // https://github.com/square/retrofit/tree/master/retrofit-converters/gson
    implementation(libs.converter.gson)
    // https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor
    implementation(libs.logging.interceptor)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    //framework ktx dependency
    implementation(libs.androidx.fragment.ktx)

    // Jetpack Navigation Kotlin
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // hilt
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    /*  Google Cloud  */
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.speech)
    implementation(libs.google.cloud.language)
    implementation(libs.grpc.google.cloud.speech.v1)

    /* gRPC (full runtime)  */
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    // https://github.com/PhilJay/MPAndroidChart
    implementation(libs.mpandroidchart)



}