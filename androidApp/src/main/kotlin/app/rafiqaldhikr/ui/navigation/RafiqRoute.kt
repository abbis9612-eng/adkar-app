package app.rafiqaldhikr.ui.navigation

/**
 * وجهة مؤجَّلة إلى ما بعد V1: الشاشة مكتوبة ومسجَّلة في الرسم البياني،
 * لكن لا مدخل لها من أي واجهة. الوسم يعفيها من فحص المسارات اليتيمة
 * في CI — فبدونه يفشل البناء عمداً على أي مسار لا يصله زرّ.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class HiddenInV1

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
    /**
     * المصحف صفحةً صفحة — وهو قارئُ القرآن الوحيد.
     *
     * وكانت معه شاشةُ قراءةٍ ثانيةٌ مبنيّةٌ على السورة، فيرى القارئ
     * تصميمين مختلفين حسب الباب الذي دخل منه. حُذفت، وصار كلُّ ما
     * يفتح القرآن — القائمةُ والبحثُ والعلامات — يفتح صفحتَه هنا.
     */
    data object Mushaf            : RafiqRoute("mushaf?page={page}&aya={aya}") {
        fun atPage(page: Int) = "mushaf?page=$page&aya="

        /**
         * صفحةٌ وآيةٌ بعينها — تُفتح الورقةُ وقد أُبرزت الآيةُ فيها.
         *
         * كان البحثُ يفتح الصفحةَ وحدَها، فتجد آيةً في الكهف فتُفتح
         * صفحةُ ٢٩٣ وعليك أن تلتقطها بعينك بين خمسةَ عشرَ سطراً.
         */
        fun atVerse(page: Int, verse: String) = "mushaf?page=$page&aya=$verse"

        /** بلا صفحة: يُفتح على آخر ما قرأ. */
        val tab = "mushaf?page=0&aya="
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

    // ═══ M1 — Others ═══
    /** ورقة اليوم — كانت الشاشة الأولى، وصارت باباً في الرئيسية. */
    data object DayPage           : RafiqRoute("day_page")
    data object Profile           : RafiqRoute("profile")
    data object Statistics        : RafiqRoute("statistics")

    // ═══ M1 — Settings ═══
    data object Settings              : RafiqRoute("settings")
    data object NotificationSettings  : RafiqRoute("notification_settings")
    data object ThemeSettings         : RafiqRoute("theme_settings")
    data object Colors                : RafiqRoute("colors")
    data object FontSettings          : RafiqRoute("font_settings")
    data object AccessibilitySettings : RafiqRoute("accessibility_settings")
    data object About             : RafiqRoute("about")
    data object Help              : RafiqRoute("help")

    // ═══ M2 — New Features ═══
    data object Breathing         : RafiqRoute("breathing")
    data object Garden            : RafiqRoute("garden")
    data object Achievements      : RafiqRoute("achievements")
    data object ShareCard         : RafiqRoute("share_card")
    data object WeeklyReport      : RafiqRoute("weekly_report")
    data object PrivacyPolicy     : RafiqRoute("privacy_policy")
    data object Terms             : RafiqRoute("terms")
    data object Contact           : RafiqRoute("contact")
    data object ExportData        : RafiqRoute("export_data")

    // ═══ M3 — Advanced ═══
    data object Language          : RafiqRoute("language")
    data object WhatsNew          : RafiqRoute("whats_new")
}
