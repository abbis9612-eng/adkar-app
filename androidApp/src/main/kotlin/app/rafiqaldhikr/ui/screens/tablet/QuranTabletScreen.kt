package app.rafiqaldhikr.ui.screens.tablet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.quran.QuranListScreen
import app.rafiqaldhikr.ui.screens.quran.QuranReadingScreen

/**
 * Tablet split layout — shows surah list on the left and reading pane on the right.
 * Requires width >= 840dp to activate in adaptive layout.
 */
@Composable
fun QuranTabletScreen(navController: NavHostController) {
    var selectedSurah by remember { mutableIntStateOf(1) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane — Surah List
        Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            QuranListScreen(navController)
        }

        // Divider
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // Right pane — Reading
        Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            QuranReadingScreen(
                surahNumber   = selectedSurah,
                navController = navController
            )
        }
    }
}
