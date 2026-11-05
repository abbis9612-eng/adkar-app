package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle

/**
 * زر الرجوع الموحد للتطبيق — بديل النسخ اليدوية المكررة في الشاشات.
 *
 * والسهمُ يتبع اتّجاهَ التخطيط: يميناً في العربية ويساراً في الإنجليزية.
 * وكان مرسوماً يميناً دائماً بحجّة أنّ «اتجاه التطبيق RTL» — وهو صحيحٌ
 * حين كُتب، ثمّ صار التطبيق يدعم الإنجليزية (`values-en/bools.xml`
 * يجعل `is_rtl = false`) فصار زرُّ الرجوع يشير إلى الأمام.
 */
@Composable
fun RafiqBackButton(
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    // كان الوصفُ «رجوع» مكتوباً في الكود، فيُقرأ عربياً في الواجهة الإنجليزية.
    val label = stringResource(R.string.action_back)
    Box(
        modifier
            .size(40.dp)
            .clip(RafiqShape.item)
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.item)
            .clickable(onClick = onClick)
            // الشكل 40dp لكن مساحة اللمس 48 — الحدّ الأدنى مهما صغُر البصري
            .minimumInteractiveComponentSize()
            .semantics {
                contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Canvas(Modifier.size(18.dp).scale(scaleX = if (rtl) 1f else -1f, scaleY = 1f)) {
            val w = size.width
            val h = size.height
            drawPath(
                Path().apply {
                    moveTo(w * 0.35f, h * 0.15f)
                    lineTo(w * 0.70f, h * 0.50f)
                    lineTo(w * 0.35f, h * 0.85f)
                },
                rc.emerald,
                style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
