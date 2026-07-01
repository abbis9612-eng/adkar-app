package app.rafiqaldhikr.di

import app.rafiq.data.db.DatabaseDriverFactory
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.usecase.GetPrayerTimesUseCase
import app.rafiq.platform.JsonResourceReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    // ═══ Platform-specific dependencies ═══
    single { DatabaseDriverFactory(androidContext()) }
    single { JsonResourceReader(androidContext()) }
    single { createEncryptedPrefs(androidContext()) }

    // ═══ Android-only Use Cases ═══
    // ✅ CLAUDE.md: GetPrayerTimesUseCase في androidModule — لأنه يعتمد على PrayerTimeCalculator
    single { PrayerTimeCalculator() }
}
