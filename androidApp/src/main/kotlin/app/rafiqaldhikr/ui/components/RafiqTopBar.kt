package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import app.rafiqaldhikr.ui.theme.RafiqType

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
    Column {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = RafiqType.titleL,
                color = rc.emerald,
            )
            if (subtitle != null) {
                Text(subtitle,
                    color = rc.inkMed, style = RafiqType.caption)
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
    // شريط الميقات — عنصر التوقيع، أسفل رأس كل شاشة
    MeeqatBar()
    Spacer(Modifier.height(6.dp))
    }
}
