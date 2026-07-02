package app.rafiqaldhikr.di

import app.rafiqaldhikr.ui.screens.adhkar.AdhkarCategoriesViewModel
import app.rafiqaldhikr.ui.screens.adhkar.DhikrReadingViewModel
import app.rafiqaldhikr.ui.screens.dua.DuaViewModel
import app.rafiqaldhikr.ui.screens.home.HomeViewModel
import app.rafiqaldhikr.ui.screens.khatira.KhatiraViewModel
import app.rafiqaldhikr.ui.screens.prayer.PrayerTimesViewModel
import app.rafiqaldhikr.ui.screens.premium.PremiumViewModel
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.screens.qibla.QiblaViewModel
import app.rafiqaldhikr.ui.screens.quran.QuranListViewModel
import app.rafiqaldhikr.ui.screens.quran.QuranReadingViewModel
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.screens.tasbeeh.TasbeehViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import app.rafiqaldhikr.ui.screens.dhikr.CustomDhikrViewModel
import app.rafiqaldhikr.ui.screens.export.ExportDataViewModel

val viewModelModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AdhkarCategoriesViewModel)
    viewModelOf(::DhikrReadingViewModel)
    viewModelOf(::TasbeehViewModel)
    viewModelOf(::QuranListViewModel)
    viewModelOf(::QuranReadingViewModel)
    viewModelOf(::PrayerTimesViewModel)  // ✅ مُضاف
    viewModelOf(::QiblaViewModel)        // ✅ مُضاف
    viewModelOf(::DuaViewModel)
    viewModelOf(::KhatiraViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::PremiumViewModel)
    viewModelOf(::CustomDhikrViewModel)
    viewModelOf(::ExportDataViewModel)
}
