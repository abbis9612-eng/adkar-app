package app.rafiq.di

import app.rafiq.data.db.DatabaseSeeder
import app.rafiq.data.db.createDatabase
import app.rafiq.data.repository.*
import app.rafiq.domain.repository.*
import app.rafiq.domain.usecase.*
import org.koin.dsl.module

val sharedModule = module {
    // ═══ Database ═══
    single { createDatabase(get()) }
    single { DatabaseSeeder(get(), get()) }

    // ═══ لا شبكة ═══
    // حُذفت RafiqApi و HttpClient: مستهلكهما الوحيد كان KhatiraRepositoryImpl،
    // وهو يكتب نصّاً دينياً قادماً من خادم فوق النص المحلي الموثَّق بلا أي
    // تحقّق. والنطاق api.rafiqaldhikr.app غير مسجَّل أصلاً — أي أن من يسجّله
    // يستطيع ضخّ نصوص في كل نسخة مثبَّتة وتُعرض على أنها موثَّقة.
    // التطبيق الآن لا يفتح اتصالاً واحداً، فوعد «لا خوادم» صار بنيةً لا وعداً.

    // ═══ Repositories ═══
    single<AdhkarRepository>   { AdhkarRepositoryImpl(get()) }
    single<QuranRepository>    { QuranRepositoryImpl(get()) }
    single<DuaRepository>      { DuaRepositoryImpl(get()) }
    single<PrayerRepository>   { PrayerRepositoryImpl(get()) }
    single<ProgressRepository> { ProgressRepositoryImpl(get()) }
    single<StreakRepository>    { StreakRepositoryImpl(get()) }
    single<PrefsRepository>    { PrefsRepositoryImpl(get()) }
    single<TasbeehRepository>  { TasbeehRepositoryImpl(get()) }
    single<CustomDhikrRepository> { app.rafiq.data.repository.CustomDhikrRepositoryImpl(get()) }
    single<UserDataRepository> { UserDataRepositoryImpl(get()) }
    single<AchievementRepository> { AchievementRepositoryImpl(get()) }
    single<DayCompanionRepository> { DayCompanionRepositoryImpl(get()) }
    single<CityRepository>     { CityRepositoryImpl(get()) }

    // ═══ Prayer Times (حساب محلي offline — commonMain) ═══
    single { app.rafiq.domain.model.PrayerTimeCalculator() }

    // ═══ Use Cases ═══
    factory { GetAdhkarByCategoryUseCase(get()) }
    factory { GetDailyProgressUseCase(get()) }
    factory { UpdateStreakUseCase(get()) }
    factory { SearchQuranUseCase(get()) }
    factory { CalculateQiblaUseCase() }
    factory { app.rafiq.domain.usecase.GetPrayerTimesUseCase(get()) }
}
