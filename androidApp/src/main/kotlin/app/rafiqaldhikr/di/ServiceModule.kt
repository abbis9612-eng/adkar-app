package app.rafiqaldhikr.di

import app.rafiqaldhikr.service.CompassManager
import app.rafiqaldhikr.service.PrayerAlarmManager
import app.rafiqaldhikr.service.PrayerRescheduler
import app.rafiqaldhikr.util.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { PrayerAlarmManager(androidContext()) }
    single { PrayerRescheduler(androidContext(), get(), get()) }
    single { CompassManager(androidContext()) }
    single { ConnectivityObserver(androidContext()) }
}
