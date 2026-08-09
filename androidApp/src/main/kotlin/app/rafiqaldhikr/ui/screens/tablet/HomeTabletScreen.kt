package app.rafiqaldhikr.ui.screens.tablet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.waraqa.WaraqaScreen
import app.rafiqaldhikr.ui.screens.prayer.PrayerTimesScreen

/**
 * تقسيم اللوحي — ورقة اليوم يميناً والمواقيت يساراً.
 * Requires width >= 840dp to activate in adaptive layout.
 */
@Composable
fun HomeTabletScreen(navController: NavHostController) {
    Row(modifier = Modifier.fillMaxSize()) {
        // اليسار — ورقة اليوم
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            WaraqaScreen(navController)
        }

        // Divider
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right pane — Prayer Times
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PrayerTimesScreen(navController)
        }
    }
}
