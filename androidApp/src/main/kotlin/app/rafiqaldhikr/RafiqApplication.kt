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
