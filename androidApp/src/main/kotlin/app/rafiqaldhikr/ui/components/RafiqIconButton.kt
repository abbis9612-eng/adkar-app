package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape

/**
 * زرّ أيقونة مربّع — كان مكرّراً باسم `PillBtn` في خمس شاشات بخمس نسخ
 * شبه متطابقة، فلم يكن أيّ إصلاح فيه يصل إلى كلّها.
 *
 * الشكل 40dp لكن مساحة اللمس 48dp عبر [minimumInteractiveComponentSize] —
 * الحدّ الأدنى الواجب مهما صغُر الشكل البصري.
 *
 * @param label وصف الزرّ لقارئ الشاشة. مطلوب: زرّ أيقونة بلا وصف هو
 *        زرّ لا وجود له عند من يستعمل TalkBack.
 */
@Composable
fun RafiqIconButton(
    onClick:  () -> Unit,
    label:    String,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        modifier
            .size(40.dp)
            .clip(RafiqShape.item)
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.item)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { content() }
}
