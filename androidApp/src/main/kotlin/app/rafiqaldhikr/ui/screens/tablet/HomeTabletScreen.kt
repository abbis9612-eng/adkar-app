package app.rafiqaldhikr.ui.screens.tablet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.home.HomeScreen
import app.rafiqaldhikr.ui.screens.prayer.PrayerTimesScreen

/**
 * Tablet split layout — shows Home on the left and Prayer Times on the right.
 * Requires width >= 840dp to activate in adaptive layout.
 */
@Composable
fun HomeTabletScreen(navController: NavHostController) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane — Home
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            HomeScreen(navController)
        }

        // Divider
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right pane — Prayer Times
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PrayerTimesScreen(navController)
        }
    }
}
