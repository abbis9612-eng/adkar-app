package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRate:    (Int) -> Unit,
    onLater:  () -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌙", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "هل أعجبك رفيق الذكر؟",
                    style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "رأيك يهمنا! ساعدنا بتقييمك لنستمر في التطوير",
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))

                // Star rating
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { selectedStars = star }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$star نجوم",
                                tint = if (star <= selectedStars)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                if (selectedStars > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (selectedStars) {
                            1    -> "😔 نأسف لذلك"
                            2    -> "🤔 سنعمل على التحسين"
                            3    -> "😊 جيد!"
                            4    -> "😄 ممتاز!"
                            else -> "🤩 جزاك الله خيراً!"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onRate(selectedStars) },
                enabled  = selectedStars > 0,
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text("تقييم")
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text("لاحقاً")
            }
        }
    )
}
