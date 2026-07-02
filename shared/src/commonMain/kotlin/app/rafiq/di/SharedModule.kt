package app.rafiq.di

import app.rafiq.data.db.DatabaseSeeder
import app.rafiq.data.db.createDatabase
import app.rafiq.data.remote.RafiqApi
import app.rafiq.data.remote.createHttpClient
import app.rafiq.data.repository.*
import app.rafiq.domain.repository.*
import app.rafiq.domain.usecase.*
import org.koin.dsl.module

val sharedModule = module {
    // ═══ Database ═══
    single { createDatabase(get()) }
    single { DatabaseSeeder(get(), get()) }

    // ═══ Network ═══
    single { createHttpClient() }
    single { RafiqApi(get()) }

    // ═══ Repositories ═══
    single<AdhkarRepository>   { AdhkarRepositoryImpl(get()) }
    single<QuranRepository>    { QuranRepositoryImpl(get()) }
    single<DuaRepository>      { DuaRepositoryImpl(get()) }
    single<PrayerRepository>   { PrayerRepositoryImpl(get()) }
    single<ProgressRepository> { ProgressRepositoryImpl(get()) }
    single<StreakRepository>    { StreakRepositoryImpl(get()) }
    single<PrefsRepository>    { PrefsRepositoryImpl(get()) }
    single<KhatiraRepository>  { KhatiraRepositoryImpl(get(), get()) }
    single<TasbeehRepository>  { TasbeehRepositoryImpl(get()) }
    single<CustomDhikrRepository> { app.rafiq.data.repository.CustomDhikrRepositoryImpl(get()) }
    single<UserDataRepository> { UserDataRepositoryImpl(get()) }

    // ═══ Prayer Times (حساب محلي offline — commonMain) ═══
    single { app.rafiq.domain.model.PrayerTimeCalculator() }

    // ═══ Use Cases ═══
    factory { GetAdhkarByCategoryUseCase(get()) }
    factory { GetDailyProgressUseCase(get()) }
    factory { UpdateStreakUseCase(get()) }
    factory { GetKhatiraUseCase(get()) }
    factory { SearchQuranUseCase(get()) }
    factory { CalculateQiblaUseCase() }
    factory { app.rafiq.domain.usecase.GetPrayerTimesUseCase(get()) }
}
