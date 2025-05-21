plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")

}


android {
    namespace = "com.example.safego"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safego"
        minSdk = 26
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.location)
    implementation(libs.play.services.fitness)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //dots indicator
    implementation (libs.dotsindicator)
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // LiveData (لو هتستخدمها برضه)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // ViewModel SavedState (لو هتستخدم State Handling)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.1")



    // مكتبات CameraX لاستخدام الكاميرا بسهولة
    implementation("androidx.camera:camera-core:1.3.1") // المكون الأساسي للكاميرا
    implementation("androidx.camera:camera-lifecycle:1.3.1") // ربط الكاميرا مع دورة حياة الـ Activity
    implementation("androidx.camera:camera-view:1.3.1") // توفير View جاهز لعرض الكاميرا
    implementation("androidx.camera:camera-extensions:1.3.1") // إضافة تحسينات للكاميرا مثل الـ HDR

    // مكتبة TensorFlow Lite لتشغيل نماذج الذكاء الاصطناعي
    implementation("org.tensorflow:tensorflow-lite:2.14.0") // أحدث إصدار متوفر


    //coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.airbnb.android:lottie:6.1.0")


    //google maps
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    //PLaces api

    implementation ("com.google.android.libraries.places:places:3.3.0")


    implementation ("com.google.maps.android:android-maps-utils:2.3.0")


// Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")


// لتحويل JSON لكائنات (لو هتستخدم GSON)
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
// Retrofit مع كوروتين
    implementation ("com.squareup.retrofit2:converter-scalars:2.11.0")

//
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("androidx.cardview:cardview:1.0.0")
    // room
    // Room components
// Room Runtime (يتضمن Room الأساسية)
    implementation("androidx.room:room-runtime:2.6.1")

    // Room Compiler (لعملية Annotation Processing)
    kapt("androidx.room:room-compiler:2.6.1")

    // Room مع دعم Kotlin Extensions و Coroutines
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.github.anastr:speedviewlib:1.6.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

}