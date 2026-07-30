package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.BorderActive
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape

/**
 * سطح البطاقة الموحّد.
 *
 * كان كل صفّ وبطاقة في التطبيق يعيد كتابة نفس السلسلة يدوياً
 * (`clip` + `background(rc.card)` + `border`)، أحياناً بظلّ وأحياناً
 * بحدّ وأحياناً بالاثنين، وبنصف قطر مختلف كل مرة. هذا المُعدِّل هو
 * المصدر الوحيد لهيئة البطاقة بعد اليوم.
 *
 * بلا ظلّ عن قصد — الفصل بالحدّ وحده. راجع AppShapes.kt.
 *
 * @param active الحالة الوحيدة التي يلمع فيها الحدّ (البطاقة الحاضرة الآن).
 */
fun Modifier.rafiqCard(
    active: Boolean = false,
    shape:  Shape = RafiqShape.card,
): Modifier = composed {
    val rc = LocalRafiqColors.current
    this
        .clip(shape)
        .background(rc.card)
        .border(1.dp, rc.gold.copy(alpha = if (active) BorderActive else BorderIdle), shape)
}

/**
 * سطح شفّاف فوق خلفية داكنة أو صورة — نفس منطق الحدّ، بلا لون بطاقة.
 * يُستخدم داخل البطاقات السينمائية حيث الخلفية ليست `rc.card`.
 */
fun Modifier.rafiqGlass(
    alpha: Float = 0.12f,
    shape: Shape = RafiqShape.item,
): Modifier = composed {
    val rc = LocalRafiqColors.current
    this
        .clip(shape)
        .background(rc.card.copy(alpha = alpha))
        .border(1.dp, rc.gold.copy(alpha = BorderIdle * 2), shape)
}

/**
 * البطاقة كمكوّن — للمواضع التي تبني عموداً من المحتوى.
 * للمواضع التي تحتاج `Row` أو `Box` استخدم [rafiqCard] مباشرة.
 */
@Composable
fun RafiqCard(
    modifier: Modifier = Modifier,
    active:   Boolean = false,
    shape:    Shape = RafiqShape.card,
    padding:  PaddingValues = PaddingValues(16.dp),
    onClick:  (() -> Unit)? = null,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .rafiqCard(active = active, shape = shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}
