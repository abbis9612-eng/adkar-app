package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

enum class GlassLevel { FULL, FAKE, SOLID }

@Composable
fun GlassCard(
    modifier:   Modifier    = Modifier,
    glassLevel: GlassLevel  = GlassLevel.FAKE,
    shape:      Shape       = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val alpha = when (glassLevel) {
        GlassLevel.FULL  -> 0.70f
        GlassLevel.FAKE  -> 0.85f
        GlassLevel.SOLID -> 0.95f
    }

    Card(
        modifier  = modifier,
        shape     = shape,
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = if (glassLevel != GlassLevel.SOLID) {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        } else null
    ) {
        Column(content = content)
    }
}
