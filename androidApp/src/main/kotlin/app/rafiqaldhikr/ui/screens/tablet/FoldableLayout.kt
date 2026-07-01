package app.rafiqaldhikr.ui.screens.tablet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

/**
 * FoldableLayout — adapts content based on screen width.
 * - Foldable unfolded (>=600dp): Two-pane layout
 * - Foldable folded (<600dp): Single-pane layout
 *
 * This is a wrapper composable used to decide layout based on window size.
 */
@Composable
fun FoldableLayout(
    navController: NavHostController,
    leftPane:  @Composable () -> Unit,
    rightPane: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isWideScreen  = configuration.screenWidthDp >= 600

    if (isWideScreen) {
        // Foldable unfolded / tablet: two panes side-by-side
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                leftPane()
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                rightPane()
            }
        }
    } else {
        // Single pane
        Box(modifier = Modifier.fillMaxSize()) {
            leftPane()
        }
    }
}
