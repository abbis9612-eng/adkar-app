package app.rafiqaldhikr.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OfflineBanner(isOnline: Boolean) {
    AnimatedVisibility(
        visible = !isOnline,
        enter   = slideInVertically() + fadeIn(),
        exit    = slideOutVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text      = "لا يوجد اتصال بالإنترنت",
                modifier  = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style     = MaterialTheme.typography.labelMedium,
                color     = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
