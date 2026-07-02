package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * شريط علوي موحد لكل الشاشات الداخلية:
 * عنوان (وعنوان فرعي اختياري) يمين، وأزرار الإجراءات + زر الرجوع يسار.
 */
@Composable
fun RafiqTopBar(
    title:    String,
    subtitle: String? = null,
    onBack:   (() -> Unit)? = null,
    actions:  @Composable RowScope.() -> Unit = {},
) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = rc.emerald,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = rc.inkMed,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            actions()
            if (onBack != null) {
                RafiqBackButton(onClick = onBack)
            }
        }
    }
}
