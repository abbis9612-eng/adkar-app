package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/* ══════════════════════════════════════════════════════════════
   الصفحةُ المصحفية

   خطُّ QCF لا يُصيّر حروفاً: كلُّ كلمةٍ رمزٌ واحد، وعرضُه مضبوطٌ في
   الخطّ ليخرج السطرُ كما خرج في الورقة. ويترتّب على ذلك ثلاثةُ أمورٍ
   تخالف كلَّ نصٍّ آخر في التطبيق:

   ١) لا فراغَ بين الرموز. الفراغُ داخلُ الرمز، فإقحامُ مسافةٍ يمدُّ
      السطرَ فيتجاوز عرضَ الورقة — وهو ما كان.

   ٢) والمقاسُ يُحسب ولا يُخمَّن. قِيست ٩٠٤٦ سطراً من الصفحات الـ٦٠٤
      كلِّها في الخطوط الـ٤٧ كلِّها: وسطيُّ السطر ١٦٫١٠em وأعرضُه
      ١٧٫٤٤em في صفحة ٥٧٧. فثابتٌ واحدٌ لكلّ المصحف يُصغّر تسعةً
      وتسعين في المئة من الصفحات لأجل واحدة — فمقاسُ كلِّ صفحةٍ من
      أعرضِ أسطرها هي، محفوظاً في بيانات التخطيط.

   ٣) ولا ضبطَ نصٍّ للطرفين. الضبطُ يمطّ المسافات، ولا مسافةَ هنا.
      فالسطرُ صفٌّ من الكلمات يوزَّع فاضلُه بينها — وهذا هو صنيعُ
      الخطّاط نفسِه في المصحف: يوسّع بين الكلمات ليستوي السطر.
      والقصيرُ — لوحُ السورة والبسملةُ وآخرُ سطرٍ في السورة — يُتوسَّط،
      إذ لو وُزّع فاضلُه لتباعدت كلمتاه إلى الحافّتين.
══════════════════════════════════════════════════════════════ */

/** ما دون هذه النسبةِ من عرضِ الورقة يُعدُّ سطراً قصيراً فيُتوسَّط. */
private const val FULL_LINE = 0.80f

@Composable
fun MushafPageView(
    page: MushafPage,
    family: androidx.compose.ui.text.font.FontFamily,
    selectedVerse: String?,
    onVerseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current

    // بدايةُ كلِّ سطرٍ في مصفوفةِ الرموز — تُحسب مرّةً لا في كلِّ تصيير.
    val starts = remember(page) {
        var n = 0
        page.l.map { row -> n.also { n += row.size } }
    }

    BoxWithConstraints(modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)) {
        val avail = maxWidth
        val fs = (avail.value / page.widestEm).sp

        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            page.l.forEachIndexed { li, row ->
                val start = starts[li]
                val natural = (page.lw.getOrNull(li) ?: 1630) / 100f * fs.value
                val full = natural >= avail.value * FULL_LINE

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        if (full) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEachIndexed { i, delta ->
                        val g = start + i
                        val type = page.t.getOrNull(g) ?: GlyphType.WORD
                        val vk = page.v.getOrNull(g).orEmpty()
                        val picked = vk.isNotEmpty() && vk == selectedVerse

                        Text(
                            text = page.glyph(delta),
                            fontFamily = family,
                            fontSize = fs,
                            color = when {
                                type == GlyphType.AYAH_END || type == GlyphType.QUARTER ->
                                    rc.goldLight
                                type == GlyphType.SURAH_HEADER || type == GlyphType.BISMILLAH ->
                                    rc.gold
                                picked -> rc.emerald
                                else -> rc.ink
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .then(
                                    if (picked) {
                                        Modifier.background(rc.goldLight.copy(alpha = 0.12f))
                                    } else {
                                        Modifier
                                    },
                                )
                                .then(
                                    if (vk.isNotEmpty()) {
                                        Modifier.clickable { onVerseClick(vk) }
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}
