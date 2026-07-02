package app.rafiqaldhikr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.rafiqaldhikr.ui.screens.about.AboutScreen
import app.rafiqaldhikr.ui.screens.achievements.AchievementsScreen
import app.rafiqaldhikr.ui.screens.adhkar.AdhkarCategoriesScreen
import app.rafiqaldhikr.ui.screens.adhkar.CelebrationScreen
import app.rafiqaldhikr.ui.screens.adhkar.DhikrReadingScreen
import app.rafiqaldhikr.ui.screens.breathing.BreathingScreen
import app.rafiqaldhikr.ui.screens.dhikr.CustomDhikrScreen
import app.rafiqaldhikr.ui.screens.dua.DuaCategoriesScreen
import app.rafiqaldhikr.ui.screens.dua.DuaListScreen
import app.rafiqaldhikr.ui.screens.dua.EmotionalDuaScreen
import app.rafiqaldhikr.ui.screens.export.ExportDataScreen
import app.rafiqaldhikr.ui.screens.garden.GardenScreen
import app.rafiqaldhikr.ui.screens.help.HelpScreen
import app.rafiqaldhikr.ui.screens.home.HomeScreen
import app.rafiqaldhikr.ui.screens.khatira.KhatiraScreen
import app.rafiqaldhikr.ui.screens.legal.ContactScreen
import app.rafiqaldhikr.ui.screens.legal.PrivacyPolicyScreen
import app.rafiqaldhikr.ui.screens.legal.TermsScreen
import app.rafiqaldhikr.ui.screens.onboarding.OnboardingScreen
import app.rafiqaldhikr.ui.screens.prayer.PrayerMethodScreen
import app.rafiqaldhikr.ui.screens.prayer.PrayerTimesScreen
import app.rafiqaldhikr.ui.screens.prayer.PrayerTrackingScreen
import app.rafiqaldhikr.ui.screens.premium.PremiumScreen
import app.rafiqaldhikr.ui.screens.profile.ProfileScreen
import app.rafiqaldhikr.ui.screens.qibla.QiblaScreen
import app.rafiqaldhikr.ui.screens.quran.QuranAudioPlayer
import app.rafiqaldhikr.ui.screens.quran.QuranBookmarksScreen
import app.rafiqaldhikr.ui.screens.quran.QuranListScreen
import app.rafiqaldhikr.ui.screens.quran.QuranReadingScreen
import app.rafiqaldhikr.ui.screens.quran.QuranSearchScreen
import app.rafiqaldhikr.ui.screens.ramadan.RamadanHomeScreen
import app.rafiqaldhikr.ui.screens.report.WeeklyReportScreen
import app.rafiqaldhikr.ui.screens.settings.AccessibilitySettingsScreen
import app.rafiqaldhikr.ui.screens.settings.FontSettingsScreen
import app.rafiqaldhikr.ui.screens.settings.NotificationSettingsScreen
import app.rafiqaldhikr.ui.screens.settings.SettingsScreen
import app.rafiqaldhikr.ui.screens.settings.ThemeSettingsScreen
import app.rafiqaldhikr.ui.screens.share.ShareCardScreen
import app.rafiqaldhikr.ui.screens.statistics.StatisticsScreen
import app.rafiqaldhikr.ui.screens.tasbeeh.TasbeehScreen
import app.rafiqaldhikr.ui.screens.widget.WidgetSettingsScreen
import app.rafiqaldhikr.ui.screens.language.LanguageScreen
import app.rafiqaldhikr.ui.screens.whatsnew.WhatsNewScreen
import app.rafiqaldhikr.ui.screens.deeplink.DeepLinkLandingScreen
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionScreen

@Composable
fun RafiqNavGraph(
    navController:       NavHostController,
    onboardingCompleted: Boolean,
    modifier:            Modifier = Modifier
) {
    // تُحسب مرة واحدة لعمر NavHost — تغيّرها بعد إتمام Onboarding مع
    // navigate() المتزامن يسبب إعادة بناء الرسم البياني وسط الانتقال
    val startDestination = androidx.compose.runtime.remember {
        if (onboardingCompleted) RafiqRoute.Home.route else RafiqRoute.Onboarding.route
    }

    NavHost(
        navController      = navController,
        startDestination   = startDestination,
        modifier           = modifier,
        enterTransition    = CinematicTransitions.enterTransition,
        exitTransition     = CinematicTransitions.exitTransition,
        popEnterTransition = CinematicTransitions.popEnterTransition,
        popExitTransition  = CinematicTransitions.popExitTransition,
    ) {
        // ═══════════════════════════════════════════
        //  M1 — Core
        // ═══════════════════════════════════════════
        composable(RafiqRoute.Onboarding.route) { OnboardingScreen(navController) }
        composable(RafiqRoute.Home.route)       { HomeScreen(navController) }

        // ═══ Adhkar ═══
        composable(RafiqRoute.AdhkarCategories.route) { AdhkarCategoriesScreen(navController) }
        composable(
            route     = RafiqRoute.DhikrReading.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://rafiqaldhikr.app/adhkar/{category}" })
        ) { entry ->
            DhikrReadingScreen(
                category      = entry.arguments?.getString("category") ?: "",
                navController = navController
            )
        }
        composable(RafiqRoute.Celebration.route) { CelebrationScreen(navController) }

        // ═══ Tasbeeh ═══
        composable(
            route     = RafiqRoute.Tasbeeh.route,
            deepLinks = listOf(navDeepLink { uriPattern = "https://rafiqaldhikr.app/tasbeeh" })
        ) { TasbeehScreen(navController) }

        // ═══ Quran ═══
        composable(RafiqRoute.QuranList.route) { QuranListScreen(navController) }
        composable(
            route     = RafiqRoute.QuranReading.route,
            arguments = listOf(navArgument("surah") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://rafiqaldhikr.app/quran/{surah}" })
        ) { entry ->
            QuranReadingScreen(
                surahNumber   = entry.arguments?.getInt("surah") ?: 1,
                navController = navController
            )
        }
        composable(RafiqRoute.QuranSearch.route)    { QuranSearchScreen(navController) }
        composable(RafiqRoute.QuranBookmarks.route) { QuranBookmarksScreen(navController) }

        // ═══ Prayer ═══
        composable(
            route     = RafiqRoute.PrayerTimes.route,
            deepLinks = listOf(navDeepLink { uriPattern = "https://rafiqaldhikr.app/prayer" })
        ) { PrayerTimesScreen(navController) }
        composable(RafiqRoute.PrayerMethod.route)  { PrayerMethodScreen(navController) }
        composable(RafiqRoute.Qibla.route)         { QiblaScreen(navController) }

        // ═══ Dua ═══
        composable(RafiqRoute.DuaCategories.route) { DuaCategoriesScreen(navController) }
        composable(
            route     = RafiqRoute.DuaList.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { entry ->
            DuaListScreen(
                category      = entry.arguments?.getString("category") ?: "",
                navController = navController
            )
        }
        composable(RafiqRoute.EmotionalDua.route) { EmotionalDuaScreen(navController) }

        // ═══ Khatira ═══
        composable(RafiqRoute.Khatira.route) { KhatiraScreen(navController) }

        // ═══ Profile ═══
        composable(RafiqRoute.Profile.route)    { ProfileScreen(navController) }
        composable(RafiqRoute.Statistics.route)  { StatisticsScreen(navController) }

        // ═══ Settings ═══
        composable(RafiqRoute.Settings.route)              { SettingsScreen(navController) }
        composable(RafiqRoute.ThemeSettings.route)         { ThemeSettingsScreen(navController) }
        composable(RafiqRoute.FontSettings.route)          { FontSettingsScreen(navController) }
        composable(RafiqRoute.NotificationSettings.route)  { NotificationSettingsScreen(navController) }
        composable(RafiqRoute.AccessibilitySettings.route) { AccessibilitySettingsScreen(navController) }

        // ═══ Premium, About, Help ═══
        composable(RafiqRoute.Premium.route) { PremiumScreen(navController) }
        composable(RafiqRoute.About.route)   { AboutScreen(navController) }
        composable(RafiqRoute.Help.route)    { HelpScreen(navController) }

        // ═══════════════════════════════════════════
        //  M2 — New Features
        // ═══════════════════════════════════════════

        // Quran Audio Player
        composable(
            route     = RafiqRoute.QuranAudioPlayer.route,
            arguments = listOf(navArgument("surah") { type = NavType.IntType })
        ) { entry ->
            QuranAudioPlayer(
                surahNumber   = entry.arguments?.getInt("surah") ?: 1,
                navController = navController
            )
        }

        // Breathing & Dhikr
        composable(RafiqRoute.Breathing.route)   { BreathingScreen(navController) }
        composable(RafiqRoute.CustomDhikr.route) { CustomDhikrScreen(navController) }

        // Gamification
        composable(RafiqRoute.Garden.route)       { GardenScreen(navController) }
        composable(RafiqRoute.Achievements.route) { AchievementsScreen(navController) }

        // Prayer Tracking
        composable(RafiqRoute.PrayerTracking.route) { PrayerTrackingScreen(navController) }

        // Social
        composable(RafiqRoute.ShareCard.route) { ShareCardScreen(navController) }

        // Ramadan
        composable(RafiqRoute.RamadanHome.route) { RamadanHomeScreen(navController) }

        // Reports
        composable(RafiqRoute.WeeklyReport.route) { WeeklyReportScreen(navController) }

        // Legal & Info
        composable(RafiqRoute.PrivacyPolicy.route) { PrivacyPolicyScreen(navController) }
        composable(RafiqRoute.Terms.route)         { TermsScreen(navController) }
        composable(RafiqRoute.Contact.route)       { ContactScreen(navController) }

        // Data & Widgets
        composable(RafiqRoute.ExportData.route)      { ExportDataScreen(navController) }
        composable(RafiqRoute.WidgetSettings.route)  { WidgetSettingsScreen(navController) }

        // ═══════════════════════════════════════════
        //  M3 — Advanced
        // ═══════════════════════════════════════════

        // Day Companion — رفيق اليوم
        composable(
            route     = RafiqRoute.DayCompanion.route,
            deepLinks = listOf(navDeepLink { uriPattern = "https://rafiqaldhikr.app/day" })
        ) { DayCompanionScreen(navController) }

        // Language
        composable(RafiqRoute.Language.route) { LanguageScreen(navController) }

        // What's New
        composable(RafiqRoute.WhatsNew.route) { WhatsNewScreen(navController) }

        // Deep Link Landing
        composable(
            route     = RafiqRoute.DeepLinkLanding.route,
            arguments = listOf(navArgument("target") { type = NavType.StringType })
        ) { entry ->
            DeepLinkLandingScreen(
                target        = entry.arguments?.getString("target") ?: "",
                navController = navController
            )
        }
    }
}
