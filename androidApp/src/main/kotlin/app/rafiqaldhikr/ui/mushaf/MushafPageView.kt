package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import androidx.compose.foundation.shape.RoundedCornerShape

/* ══════════════════════════════════════════════════════════════
   الصفحةُ المصحفية

   سطرٌ واحدٌ لكلِّ سطرٍ في الورقة، والأسطرُ موزَّعةٌ على ارتفاع الصفحة.
   ولا لفَّ ولا كسر: عرضُ السطر يُضبط بالمقاس لا بقطع الكلمات — فإن
   ضاق الجهازُ صغُر الخطُّ ولم ينكسر السطر، لأنّ كسرَه يُبطل كونَ
   الصفحة مصحفية.

   وعلاماتُ نهاية الآية ولوحُ السورة رموزٌ في الخطّ نفسِه لا رسومٌ
   نضعها — ولذلك تُلوَّن بلونٍ آخر ولا تُرسم.
══════════════════════════════════════════════════════════════ */

@Composable
fun MushafPageView(
    page: MushafPage,
    family: FontFamily,
    fontSize: TextUnit,
    selectedVerse: String?,
    onVerseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    var idx = 0

    Column(
        modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        page.l.forEach { row ->
            val start = idx
            idx += row.size
            // مفاتيحُ الآيات في هذا السطر — لمسةُ السطر تختار أقربَها
            val keys = (start until idx).mapNotNull { page.v.getOrNull(it)?.takeIf(String::isNotEmpty) }
            val isHeader = (start until idx).all {
                page.t.getOrNull(it) == GlyphType.SURAH_HEADER
            }
            val text = buildAnnotatedString {
                row.forEachIndexed { i, delta ->
                    val g = start + i
                    val type = page.t.getOrNull(g) ?: GlyphType.WORD
                    val vk = page.v.getOrNull(g).orEmpty()
                    val color = when (type) {
                        GlyphType.AYAH_END, GlyphType.QUARTER -> rc.goldLight
                        GlyphType.SURAH_HEADER -> rc.gold
                        else -> if (vk.isNotEmpty() && vk == selectedVerse) rc.emerald else rc.ink
                    }
                    withStyle(SpanStyle(color = color)) { append(page.glyph(delta)) }
                    if (i < row.lastIndex) append(" ")
                }
            }
            Text(
                text = text,
                fontFamily = family,
                fontSize = if (isHeader) fontSize * 1.05f else fontSize,
                textAlign = if (isHeader) TextAlign.Center else TextAlign.Justify,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (keys.isNotEmpty()) Modifier.clickable { onVerseClick(keys.first()) }
                        else Modifier,
                    )
                    .then(
                        if (selectedVerse != null && keys.contains(selectedVerse))
                            Modifier.background(rc.goldLight.copy(alpha = 0.10f))
                        else Modifier,
                    )
                    .padding(horizontal = 2.dp),
            )
        }
    }
}
