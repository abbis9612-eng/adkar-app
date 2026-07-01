# ═══ SQLDelight ═══
-keep class app.rafiq.db.** { *; }

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
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ═══ Koin ═══
-keep class org.koin.** { *; }

# ═══ RevenueCat ═══
-keep class com.revenuecat.** { *; }

# ═══ Firebase Crashlytics ═══
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ═══ Coroutines ═══
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ═══ Services & Widgets ═══
-keep class app.rafiqaldhikr.service.** { *; }
-keep class app.rafiqaldhikr.widget.**  { *; }
