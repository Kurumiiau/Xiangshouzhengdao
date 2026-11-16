plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

android {
    namespace = "com.example.xiangshouzhengdao"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.xiangshouzhengdao"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "2.1.4"

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
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    
    // Gson - JSON解析
    implementation(libs.gson)
    
    // Room - 本地数据库 (使用KSP替代kapt)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // MPAndroidChart - 雷达图
    implementation(libs.mpandroidchart)
    
    // Lifecycle - ViewModel + LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // ExifInterface - 图片旋转处理
    implementation(libs.androidx.exifinterface)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
