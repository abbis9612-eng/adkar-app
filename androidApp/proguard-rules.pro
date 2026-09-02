# ═══ SQLDelight ═══
# النوعُ المولَّد يُبنى بالانعكاس في مسار المحرِّك، فيبقى محفوظاً.
-keep class app.rafiq.db.** { *; }

# ═══ Glance / AppWidget ═══
# مدخلا الودجت يُنشَآن بالاسم من النظام لا من كودنا.
-keep class app.rafiqaldhikr.widget.PrayerWidget { *; }
-keep class app.rafiqaldhikr.widget.PrayerWidgetReceiver { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# ═══ Kotlinx Serialization ═══
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.rafiq.**$$serializer { *; }
-keepclassmembers class app.rafiq.** {
    *** Companion;
}
-keepclasseswithmembers class app.rafiq.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ═══ Ktor ═══
# حُذفت: Ktor أُزيلت من المشروع (انظر di/SharedModule.kt) — والقاعدةُ
# باقيةٌ تحرس مكتبةً غير موجودة.

# ═══ Koin ═══
-keep class org.koin.** { *; }

# ═══ RevenueCat ═══
# حُذفت: ليست تبعيّةً في المشروع.

# ═══ Firebase Crashlytics ═══
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ═══ Coroutines ═══
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ═══ Services & Widgets ═══
-keep class app.rafiqaldhikr.service.** { *; }
-keep class app.rafiqaldhikr.widget.**  { *; }
