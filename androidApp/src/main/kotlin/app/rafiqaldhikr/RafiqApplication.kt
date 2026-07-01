package app.rafiqaldhikr

import android.app.Application
import app.rafiq.di.sharedModule
import app.rafiqaldhikr.di.androidModule
import app.rafiqaldhikr.di.serviceModule
import app.rafiqaldhikr.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

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
