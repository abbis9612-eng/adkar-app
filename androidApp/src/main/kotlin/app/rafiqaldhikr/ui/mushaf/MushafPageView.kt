package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ══════════════════════════════════════════════════════════════
   الصفحةُ المصحفية

   خطُّ QCF لا يُصيّر حروفاً: كلُّ كلمةٍ رمزٌ واحد، وعرضُه مضبوطٌ في
   الخطّ ليخرج السطرُ كما خرج في الورقة. ويترتّب على ذلك أربعةُ أمورٍ
   تخالف كلَّ نصٍّ آخر في التطبيق:

   ١) لا فراغَ بين الرموز. الفراغُ داخلُ الرمز، فإقحامُ مسافةٍ يمدُّ
      السطرَ فيتجاوز عرضَ الورقة.

   ٢) والمقاسُ يُحسب ولا يُخمَّن. قِيست ٩٠٤٦ سطراً من الصفحات الـ٦٠٤
      كلِّها في الخطوط الـ٤٧ كلِّها: وسطيُّ السطر ١٦٫١٠em وأعرضُه
      ١٧٫٤٤em في صفحة ٥٧٧. فمقاسُ كلِّ صفحةٍ من أعرضِ أسطرِ متنِها هي،
      محفوظاً في بيانات التخطيط.

   ٣) ولا ضبطَ نصٍّ للطرفين. الضبطُ يمطّ المسافات، ولا مسافةَ هنا.
      فالسطرُ صفٌّ من الكلمات يوزَّع فاضلُه بينها — وهذا صنيعُ الخطّاط
      نفسِه: يوسّع بين الكلمات ليستوي السطر.

   ٤) واللوحُ والبسملةُ لهما خطّان آخران ومقاسان آخران. اللوحُ على
      QCF4_QBSML والبسملةُ على QCF4_Hafs_01 ثابتاً، وعرضُهما ٢٫٦–٤٫٣em
      و٦٫٤–١٠٫١em — أي دون ربع السطر. فلو رُسما بمقاس المتن لظهرا
      كلمتين تائهتين في وسط الورقة، ولذلك يُقاسان إلى نسبةٍ من عرضها.

   والورقةُ نفسُها من مصحفٍ مجلَّد: هامشُها الخارجيُّ أوسعُ من الداخليّ،
   والكعبُ يُلقي ظلاًّ. والفرديّةُ في الكتاب العربيّ يسارَ الفتحة فكعبُها
   عن يمينها، والزوجيّةُ عكسُها. وهذه الذاكرةُ المكانيّة هي ما يعتمد
   عليه الحافظ حين يقول «الآيةُ في أعلى الصفحة اليمنى» — وتضيع حين
   تُسوَّى الهوامش كما في سائر التطبيقات.
══════════════════════════════════════════════════════════════ */

/** نسبةُ عرضِ الورقة التي يملؤها لوحُ السورة. */
private const val PLATE_FILL = 0.52f

/** ونسبةُ البسملة — أوسعُ قليلاً لأنّها جملةٌ لا اسم. */
private const val BISM_FILL = 0.60f

/** ما دون هذه النسبةِ من عرضِ الورقة سطرٌ قصيرٌ يُتوسَّط. */
private const val FULL_LINE = 0.80f

private val MARGIN_OUTER = 26.dp
private val MARGIN_INNER = 15.dp
private val SPINE_WIDTH = 22.dp

@Composable
fun MushafPageView(
    page: MushafPage,
    fonts: PageFonts,
    ink: Color,
    accent: Color,
    marker: Color,
    selectedVerse: String?,
    onVerseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // بدايةُ كلِّ سطرٍ وأنواعُه — تُحسب مرّةً لا في كلِّ تصيير.
    val starts = remember(page) {
        var n = 0
        page.l.map { row -> n.also { n += row.size } }
    }
    val lineTypes = remember(page) { page.l.indices.map { page.typesOf(it) } }
    // أعرضُ سطرِ متنٍ — واللوحُ والبسملةُ خارجَه لأنّ em خطِّهما آخر.
    val widestEm = remember(page) {
        var w = 0
        page.l.indices.forEach { li ->
            val types = page.typesOf(li)
            if (GlyphType.SURAH_HEADER !in types && GlyphType.BISMILLAH !in types) {
                w = maxOf(w, page.lw.getOrNull(li) ?: 0)
            }
        }
        (if (w == 0) 1630 else w) / 100f
    }

    val odd = page.p % 2 == 1
    val pad = if (odd) {
        PaddingValues(start = MARGIN_INNER, end = MARGIN_OUTER)
    } else {
        PaddingValues(start = MARGIN_OUTER, end = MARGIN_INNER)
    }

    Box(modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(pad)) {
            val avail = maxWidth.value
            val fs = (avail / widestEm).sp

            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                page.l.forEachIndexed { li, row ->
                    val start = starts[li]
                    val types = lineTypes[li]
                    val em = (page.lw.getOrNull(li) ?: 1630) / 100f

                    val plate = GlyphType.SURAH_HEADER in types
                    val bism = GlyphType.BISMILLAH in types
                    val family = when {
                        plate -> fonts.plate ?: fonts.body
                        bism -> fonts.bism ?: fonts.body
                        else -> fonts.body
                    }
                    val size = when {
                        plate -> (avail * PLATE_FILL / em).sp
                        bism -> (avail * BISM_FILL / em).sp
                        else -> fs
                    }
                    val fills = !plate && !bism && em * fs.value >= avail * FULL_LINE

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            if (fills) Arrangement.SpaceBetween else Arrangement.Center,
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
                                fontSize = size,
                                color = when {
                                    type == GlyphType.AYAH_END || type == GlyphType.QUARTER -> marker
                                    plate || bism -> accent
                                    picked -> accent
                                    else -> ink
                                },
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .then(
                                        if (picked) {
                                            Modifier.background(marker.copy(alpha = 0.15f))
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
        /*  في التخطيط من اليمين إلى اليسار، الـ`start` هو اليمين. والصفحةُ
            الفرديّة تقع يسارَ الفتحة فكعبُها عن يمينها — أي عند الـ`start`. */
        SpineShadow(
            odd = odd,
            modifier = Modifier.align(if (odd) Alignment.CenterStart else Alignment.CenterEnd),
        )
    }
}

/** ظلُّ الكعب — الحافّةُ المجلَّدةُ من الصفحة. */
@Composable
private fun SpineShadow(odd: Boolean, modifier: Modifier = Modifier, width: Dp = SPINE_WIDTH) {
    val dark = Color.Black.copy(alpha = 0.085f)
    Box(
        modifier
            .width(width)
            .fillMaxHeight()
            .background(
                if (odd) {
                    Brush.horizontalGradient(listOf(Color.Transparent, dark))
                } else {
                    Brush.horizontalGradient(listOf(dark, Color.Transparent))
                },
            ),
    )
}
