package app.rafiqaldhikr.di

import app.rafiqaldhikr.ui.screens.adhkar.AdhkarCategoriesViewModel
import app.rafiqaldhikr.ui.screens.adhkar.DhikrReadingViewModel
import app.rafiqaldhikr.ui.screens.dua.DuaViewModel
import app.rafiqaldhikr.ui.screens.home.HomeViewModel
import app.rafiqaldhikr.ui.screens.prayer.PrayerTimesViewModel
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.screens.qibla.QiblaViewModel
import app.rafiqaldhikr.ui.screens.quran.QuranListViewModel
import app.rafiqaldhikr.ui.screens.quran.QuranReadingViewModel
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.screens.tasbeeh.TasbeehViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import app.rafiqaldhikr.ui.screens.export.ExportDataViewModel
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel
import app.rafiqaldhikr.ui.components.MeeqatViewModel
import app.rafiqaldhikr.ui.components.LocationRequestViewModel
import app.rafiqaldhikr.ui.screens.profile.DaysGridViewModel

val viewModelModule = module {
    // طلب الموقع — تستعمله كل شاشة تحتاج إحداثيات (المواقيت، القبلة، الورقة)
    viewModelOf(::LocationRequestViewModel)
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
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ExportDataViewModel)
    viewModelOf(::DayCompanionViewModel)
    viewModelOf(::MeeqatViewModel)
    viewModelOf(::DaysGridViewModel)
}
