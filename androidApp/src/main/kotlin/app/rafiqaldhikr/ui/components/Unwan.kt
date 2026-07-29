package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.AmiriFamily
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * العُنوان — لوح الصدر المذهَّب، ثالث مفردات نظام التذهيب. يُستخدم حصراً في رأسَي
 * الشاشة الرئيسية وشاشة "رفيق اليوم" — لا يتكرّر في أي شاشة فرعية أخرى، فيبقى استثنائياً.
 */
@Composable
fun Unwan(
    modifier: Modifier = Modifier,
    text: String = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
) {
    val rc = LocalRafiqColors.current
    Box(modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().height(72.dp)) {
            val w = size.width
            val h = size.height
            val domeTop = h * 0.10f
            val path = Path().apply {
                moveTo(w * 0.06f, h)
                lineTo(w * 0.06f, domeTop + h * 0.30f)
                cubicTo(
                    w * 0.06f, domeTop * 0.2f,
                    w * 0.36f, 0f,
                    w * 0.5f, 0f,
                )
                cubicTo(
                    w * 0.64f, 0f,
                    w * 0.94f, domeTop * 0.2f,
                    w * 0.94f, domeTop + h * 0.30f,
                )
                lineTo(w * 0.94f, h)
                close()
            }
            drawPath(
                path,
                brush = Brush.verticalGradient(
                    listOf(rc.gold.copy(alpha = 0.16f), rc.gold.copy(alpha = 0.02f))
                ),
            )
            drawPath(path, color = rc.gold.copy(alpha = 0.45f), style = Stroke(width = 1.2.dp.toPx()))
        }
        GeomStar(s = 20.dp, color = rc.gold, op = 0.55f, m = Modifier.padding(bottom = 44.dp))
        Text(
            text,
            fontFamily = AmiriFamily,
            fontSize = 17.sp,
            color = rc.gold,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}
