package app.rafiqaldhikr

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.rafiqaldhikr.ui.navigation.RafiqBottomBar
import app.rafiqaldhikr.ui.navigation.RafiqNavGraph
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash while prefs haven't loaded yet
        splash.setKeepOnScreenCondition {
            settingsViewModel.onboardingCompleted.value == null
        }

        setContent {
            val onboardingCompleted by settingsViewModel.onboardingCompleted
                .collectAsStateWithLifecycle()
            val dynamicColor by settingsViewModel.dynamicColor
                .collectAsStateWithLifecycle()
            val themePref by settingsViewModel.theme
                .collectAsStateWithLifecycle()
            val arabicNumerals by settingsViewModel.arabicNumerals
                .collectAsStateWithLifecycle()

            // Don't render until we know onboarding state
            if (onboardingCompleted == null) return@setContent

            val darkTheme = when (themePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            RafiqTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
              androidx.compose.runtime.CompositionLocalProvider(
                  app.rafiqaldhikr.ui.utils.LocalArabicNumerals provides arabicNumerals
              ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Bottom bar visible except on onboarding and celebration
                val showBottomBar = currentRoute !in listOf(
                    RafiqRoute.Onboarding.route,
                    RafiqRoute.Celebration.route,
                    RafiqRoute.QuranReading.route,
                    RafiqRoute.DhikrReading.route
                )

                Scaffold(
                    containerColor = LocalRafiqColors.current.bg,
                    // الشاشات تتكفل بـ statusBarsPadding بنفسها — منع ازدواج الحشوة
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomBar) {
                            RafiqBottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    RafiqNavGraph(
                        navController       = navController,
                        onboardingCompleted = onboardingCompleted!!,
                        modifier            = Modifier.padding(innerPadding)
                    )
                }
              }
            }
        }
    }
}
