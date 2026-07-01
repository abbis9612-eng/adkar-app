plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    // TODO: Uncomment when google-services.json is added
    // alias(libs.plugins.googleServices)
    // alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace  = "app.rafiqaldhikr"
    compileSdk = 36

    defaultConfig {
        applicationId         = "app.rafiqaldhikr"
        minSdk                = 23
        targetSdk             = 35
        versionCode           = 1
        versionName           = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            if (keystoreFile != null) {
                storeFile     = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias      = System.getenv("KEY_ALIAS")
                keyPassword   = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig = if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(project(":shared"))

    // ═══ Compose (BOM controls all versions) ═══
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)

    // ═══ AndroidX ═══
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.savedstate)
    implementation(libs.datastore.preferences)
    implementation(libs.appcompat)

    // ═══ Koin ═══
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // ═══ Coroutines ═══
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // ═══ Work Manager ═══
    implementation(libs.work.runtime)

    // ═══ Glance Widgets ═══
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // ═══ Media3 ═══
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)

    // ═══ Location ═══
    implementation(libs.play.services.location)

    // ═══ Security ═══
    implementation(libs.security.crypto)

    // ═══ In-App ═══
    implementation(libs.app.update.ktx)
    implementation(libs.review.ktx)

    // ═══ Firebase ═══
    // TODO: Uncomment when google-services.json is added
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.crashlytics)

    // ═══ Monetization ═══
    // TODO: Uncomment when RevenueCat API key is configured
    // implementation(libs.revenuecat)
    // implementation(libs.revenuecat.ui)

    // ═══ Testing ═══
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
