package app.rafiqaldhikr.di

import app.rafiqaldhikr.service.CompassManager
import app.rafiqaldhikr.service.PrayerAlarmManager
import app.rafiqaldhikr.util.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { PrayerAlarmManager(androidContext()) }
    single { CompassManager(androidContext()) }
    single { ConnectivityObserver(androidContext()) }

    // M2: QuranAudioService — MediaSessionService has its own lifecycle
    // Do not register directly in Koin — use via Intent from ViewModel
}
