package app.rafiqaldhikr

import android.app.Application
import app.rafiq.di.sharedModule
import app.rafiqaldhikr.di.androidModule
import app.rafiqaldhikr.di.serviceModule
import app.rafiqaldhikr.di.viewModelModule
import app.rafiqaldhikr.service.PrayerRescheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.GlobalContext

class RafiqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // ═══ لغة التطبيق الافتراضية: العربية (المحتوى عربي أولاً) ═══
        if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().isEmpty) {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags("ar")
            )
        }

        // ═══ Koin DI ═══
        startKoin {
            androidContext(this@RafiqApplication)
            modules(
                sharedModule,
                androidModule,
                viewModelModule,
                serviceModule
            )
        }

        // ═══ تعبئة قاعدة البيانات (القرآن والأذكار والأدعية والتفسير) ═══
        // الشاشات تراقب Flows فتمتلئ تلقائياً فور اكتمال التعبئة
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<app.rafiq.data.db.DatabaseSeeder>().seedIfNeeded()
            } catch (e: Exception) {
                android.util.Log.e("RafiqSeeder", "فشل تعبئة قاعدة البيانات", e)
            }
        }

        // ═══ جدولة إشعارات الأذان عند كل فتح للتطبيق ═══
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (_: Exception) {
                // offline — ستُعاد المحاولة من شاشة المواقيت أو عند الإقلاع
            }
        }

        // TODO: Firebase Crashlytics — add google-services.json first
        // FirebaseApp.initializeApp(this)

        // TODO: RevenueCat — add API key in local.properties
        // Purchases.debugLogsEnabled = BuildConfig.DEBUG
        // Purchases.configure(
        //     PurchasesConfiguration.Builder(
        //         context = this,
        //         apiKey  = BuildConfig.REVENUECAT_API_KEY
        //     ).build()
        // )
    }
}
