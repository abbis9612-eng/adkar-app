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
        targetSdk             = 36
        // رقم إصدار فريد لكل بناء CI (رقم تشغيل GitHub) حتى يقبل أندرويد التحديث دائماً.
        // محلياً يبقى 1. الاسم يحمل معرّف الكومِت حتى تُعرف النسخة بالضبط.
        versionCode           = (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1)
        versionName           = "1.0.0." +
            (System.getenv("GITHUB_RUN_NUMBER") ?: "dev") +
            " (" + (System.getenv("GITHUB_SHA")?.take(7) ?: "local") + ")"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("KEYSTORE_FILE")
                ?: providers.gradleProperty("KEYSTORE_FILE").orNull
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
            // السقوط الصامت إلى مفتاح debug كان يُخرج نسخة "إصدارية" لا
            // يقبلها المتجر ولا يمكن تحديثها لاحقاً. الآن البناء يفشل بصوت عالٍ.
            signingConfig = signingConfigs.getByName("release")
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

    /* ═══════════════════════════════════════════════════════════════
       حزمة AAB — الصيغة الوحيدة التي يقبلها Play للتطبيقات الجديدة

       يقسّم Play الحزمة إلى APKs حسب الجهاز، فينزّل المستخدم ما يخصّ
       جهازه وحده. مع أصولنا (مصحف وتفسير ٦ ميغابايت) الفرق ملموس.

       اللغة لا تُقسَّم: من يثبّت التطبيق بجهاز إنجليزي ثم يبدّل داخل
       التطبيق إلى العربية يجب أن يجد العربية موجودة.
    ═══════════════════════════════════════════════════════════════ */
    bundle {
        language { enableSplit = false }
        density  { enableSplit = true  }
        abi      { enableSplit = true  }
    }

    lint {
        // checkDependencies يجرّ :shared إلى نفس التحليل ويضاعف الزمن بلا
        // فائدة — للوحدة المشتركة فحصها الخاص.
        checkDependencies = false
        abortOnError      = true
        // نبدأ بما يُسقِط التطبيق أو يُرفَض في المتجر، لا بأسلوب الكود.
        // كل تحذير آخر يبقى مرئياً في التقرير دون أن يفشل البناء.
        checkReleaseBuilds = false
        warningsAsErrors   = false
        // التقرير كاملاً في سجل CI — بدونه يطبع Gradle أول خطأ فقط،
        // فتُصلَّح المشاكل واحدةً واحدة عبر دورات بناء متتالية.
        textReport = true
        // ٧١ تحذيراً تُغرق السجل فلا تظهر الأخطاء الـ١٧. الأخطاء أولاً،
        // والتحذيرات دفعةً لاحقة.
        ignoreWarnings = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

/* ═══════════════════════════════════════════════════════════════════
   حاجز التوقيع — لا نسخة إصدارية بمفتاح debug

   كان البناء يسقط صامتاً إلى مفتاح debug عند غياب KEYSTORE_FILE، فيُخرج
   APK يبدو إصدارياً ولا يقبله المتجر ولا يمكن تحديثه لاحقاً.

   المحاولة الأولى وضعت الفحص في doFirst على assemble/bundleRelease —
   ولم تعمل: bundleRelease مهمّة دورة حياة تعتمد على signReleaseBundle،
   فينهار التوقيع أولاً بـ NullPointerException عارٍ قبل أن يصل الفحص.
   كشفَ ذلك اختبارُ CI الذي يشغّل bundleRelease بلا مفتاح ويطالب بالرسالة
   المتوقَّعة بالذات — لا بمجرّد الفشل.

   الآن مهمّة تحقّق مستقلّة تعتمد عليها كل مهام حزم الإصدار وتوقيعها،
   فتعمل قبلها جميعاً.
═══════════════════════════════════════════════════════════════════ */
val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    doFirst {
        check(android.signingConfigs.getByName("release").storeFile != null) {
            "بناء إصدارية بلا مفتاح توقيع. حدّد KEYSTORE_FILE و KEYSTORE_PASSWORD " +
            "و KEY_ALIAS و KEY_PASSWORD كمتغيّرات بيئة قبل bundleRelease."
        }
    }
}

tasks.matching {
    it.name.endsWith("Release") && (
        it.name.startsWith("sign")     || it.name.startsWith("package") ||
        it.name.startsWith("assemble") || it.name.startsWith("bundle")
    )
}.configureEach { dependsOn(verifyReleaseSigning) }

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

    // ═══ Location ═══
    implementation(libs.play.services.location)

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
