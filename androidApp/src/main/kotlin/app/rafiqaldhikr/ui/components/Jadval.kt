package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * درجة الجَدْوَل — المفردة الأولى من نظام التذهيب. إطارٌ ذهبيّ حول محتوًى نصّيّ ديني
 * (ذِكر، دعاء، آية) لا حول أيّ بطاقة بيانات. [Double] حصراً لصفحات القراءة الكاملة.
 */
enum class JadvalIntensity { Single, Double }

/**
 * يضيف إطار الجَدْوَل الذهبي فوق الشكل الحالي للعنصر.
 * [Single]: خطٌّ واحد على حافّة البطاقة (نفس ما كان مكرَّراً يدوياً في عدّة شاشات).
 * [Double]: خطٌّ على الحافّة + خطٌّ داخليٌّ أرفع بفارق ٩dp — انضباط صفحة المصحف.
 */
fun Modifier.jadval(
    intensity: JadvalIntensity = JadvalIntensity.Single,
    corner: Dp = 24.dp,
    color: Color? = null,
): Modifier = composed {
    val rc = LocalRafiqColors.current
    val c = color ?: rc.gold
    val outer = this.border(1.dp, c.copy(alpha = 0.18f), RoundedCornerShape(corner))
    when (intensity) {
        JadvalIntensity.Single -> outer
        JadvalIntensity.Double -> outer.drawWithContent {
            drawContent()
            val inset = 9.dp.toPx()
            val innerCorner = (corner.toPx() - inset).coerceAtLeast(4f)
            drawRoundRect(
                color = c.copy(alpha = 0.30f),
                topLeft = Offset(inset, inset),
                size = Size(
                    (size.width - inset * 2).coerceAtLeast(0f),
                    (size.height - inset * 2).coerceAtLeast(0f),
                ),
                cornerRadius = CornerRadius(innerCorner),
                style = Stroke(width = 0.8.dp.toPx()),
            )
        }
    }
}
