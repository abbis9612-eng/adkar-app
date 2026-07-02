package app.rafiqaldhikr.ui.navigation

sealed class RafiqRoute(val route: String) {
    // ═══ M1 — Core ═══
    data object Onboarding        : RafiqRoute("onboarding")
    data object Home              : RafiqRoute("home")

    // ═══ M1 — Adhkar ═══
    data object AdhkarCategories  : RafiqRoute("adhkar_categories")
    data object DhikrReading      : RafiqRoute("dhikr_reading/{category}") {
        fun withCategory(category: String) = "dhikr_reading/$category"
    }
    data object Celebration       : RafiqRoute("celebration")

    // ═══ M1 — Tasbeeh ═══
    data object Tasbeeh           : RafiqRoute("tasbeeh")

    // ═══ M1 — Quran ═══
    data object QuranList         : RafiqRoute("quran_list")
    data object QuranReading      : RafiqRoute("quran_reading/{surah}") {
        fun withSurah(number: Int) = "quran_reading/$number"
    }
    data object QuranSearch       : RafiqRoute("quran_search")
    data object QuranBookmarks    : RafiqRoute("quran_bookmarks")

    // ═══ M1 — Prayer ═══
    data object PrayerTimes       : RafiqRoute("prayer_times")
    data object PrayerMethod      : RafiqRoute("prayer_method")
    data object Qibla             : RafiqRoute("qibla")

    // ═══ M1 — Dua ═══
    data object DuaCategories     : RafiqRoute("dua_categories")
    data object DuaList           : RafiqRoute("dua_list/{category}") {
        fun withCategory(category: String) = "dua_list/$category"
    }
    data object EmotionalDua      : RafiqRoute("emotional_dua")

    // ═══ M1 — Others ═══
    data object Khatira           : RafiqRoute("khatira")
    data object Profile           : RafiqRoute("profile")
    data object Statistics        : RafiqRoute("statistics")

    // ═══ M1 — Settings ═══
    data object Settings              : RafiqRoute("settings")
    data object NotificationSettings  : RafiqRoute("notification_settings")
    data object ThemeSettings         : RafiqRoute("theme_settings")
    data object FontSettings          : RafiqRoute("font_settings")
    data object AccessibilitySettings : RafiqRoute("accessibility_settings")
    data object Premium           : RafiqRoute("premium")
    data object About             : RafiqRoute("about")
    data object Help              : RafiqRoute("help")

    // ═══ M2 — New Features ═══
    data object QuranAudioPlayer  : RafiqRoute("quran_audio/{surah}") {
        fun withSurah(number: Int) = "quran_audio/$number"
    }
    // TafsirSheet هو ModalBottomSheet — يُستخدم مباشرة من QuranReadingScreen وليس وجهة تنقل
    data object Breathing         : RafiqRoute("breathing")
    data object Garden            : RafiqRoute("garden")
    data object Achievements      : RafiqRoute("achievements")
    data object PrayerTracking    : RafiqRoute("prayer_tracking")
    data object CustomDhikr       : RafiqRoute("custom_dhikr")
    data object ShareCard         : RafiqRoute("share_card")
    data object RamadanHome       : RafiqRoute("ramadan")
    data object WeeklyReport      : RafiqRoute("weekly_report")
    data object PrivacyPolicy     : RafiqRoute("privacy_policy")
    data object Terms             : RafiqRoute("terms")
    data object Contact           : RafiqRoute("contact")
    data object ExportData        : RafiqRoute("export_data")
    data object WidgetSettings    : RafiqRoute("widget_settings")

    // ═══ M3 — Advanced ═══
    data object DayCompanion      : RafiqRoute("day_companion")
    data object Language          : RafiqRoute("language")
    data object WhatsNew          : RafiqRoute("whats_new")
    data object DeepLinkLanding   : RafiqRoute("deeplink/{target}") {
        fun withTarget(target: String) = "deeplink/$target"
    }
}
