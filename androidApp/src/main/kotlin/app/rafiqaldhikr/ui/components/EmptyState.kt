package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    message:  String,
    modifier: Modifier    = Modifier
) {
    // اللوحةُ لوحةُ التطبيق — انظر التعليق في [ErrorState].
    val rc = LocalRafiqColors.current
    Column(
        modifier              = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        IcoInbox(64.dp, rc.inkLight)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = message,
            style     = RafiqType.body,
            color     = rc.inkMed,
            textAlign = TextAlign.Center
        )
    }
}
