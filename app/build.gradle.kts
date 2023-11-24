plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
}

android {
  namespace = "com.appdev.yummly2"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.appdev.yummly2"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    viewBinding = true
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {
  implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
  implementation("com.google.firebase:firebase-analytics:21.5.0")
  implementation("com.google.firebase:firebase-storage:20.3.0")
  implementation("com.firebaseui:firebase-ui-database:8.0.2")
  implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.19")
  implementation("de.hdodenhof:circleimageview:3.1.0")
  implementation("com.github.bumptech.glide:glide:4.16.0")
  implementation("com.orhanobut:dialogplus:1.11@aar")

  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.10.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("com.google.firebase:firebase-database:20.3.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}