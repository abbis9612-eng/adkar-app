package app.rafiqaldhikr.di

import app.rafiq.data.db.DatabaseDriverFactory
import app.rafiq.platform.JsonResourceReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    // ═══ Platform-specific dependencies ═══
    single { DatabaseDriverFactory(androidContext()) }
    single { JsonResourceReader(androidContext()) }
    single { createEncryptedPrefs(androidContext()) }
}
