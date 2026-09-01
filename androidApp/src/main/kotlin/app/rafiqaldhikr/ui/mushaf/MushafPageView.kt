package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/* ══════════════════════════════════════════════════════════════
   الصفحةُ المصحفية

   خطُّ QCF لا يُصيّر حروفاً: كلُّ كلمةٍ رمزٌ واحد، وعرضُه مضبوطٌ في
   الخطّ ليخرج السطرُ كما خرج في الورقة. ويترتّب على ذلك:

   ١) لا فراغَ بين الرموز — الفراغُ داخلَ الرمز.

   ٢) والمقاسُ يُحسب من بُعدين لا من واحد:

      عرضاً: قِيست ٩٠٤٦ سطراً في الصفحات الـ٦٠٤ عبر الخطوط الـ٤٧،
      فوسطيُّ السطر ١٦٫١٠em وأعرضُه ١٧٫٤٤em. فمقاسُ كلِّ صفحةٍ من
      أعرضِ أسطرِ متنِها هي.

      وارتفاعاً: صندوقُ السطر في هذا الخطّ ٢٫٢٧٧em — مقيسٌ من جدول
      `hhea`: (٣٧٠٦ + ١٩٨٦) ÷ ٢٥٠٠. وهو أوسعُ من صندوق أيّ خطٍّ
      عاديٍّ لأنّ التشكيلَ القرآنيَّ يعلو ويسفل. فخمسةَ عشرَ سطراً
      بمقاسٍ مشتقٍّ من العرض وحدَه تتجاوز ارتفاعَ الورقة على الشاشات
      الضيّقة فيُقصّ آخرُها — وهو ما وقع. فالمقاسُ أصغرُ المشتقَّين.

   ٣) والضبطُ بتوسيع ما بين الكلمات — وهو صنيعُ الخطّاط نفسِه.
      و`letterSpacing` هو الوسيلة، لأنّ كلَّ رمزٍ كلمةٌ فما بين
      الرمزين هو ما بين الكلمتين.

      وكان السطرُ صفَّ نصوصٍ منفصلة، فبلغت الصفحةُ مئةً وخمسين نصّاً
      يُقاس كلٌّ منها وحدَه، وثلاثَ صفحاتٍ حيّةً في المقلِّب. فصار
      السطرُ نصّاً واحداً — خمسةَ عشرَ بدل مئةٍ وخمسين — واللمسُ
      يُحدَّد بـ`getOffsetForPosition` على نتيجة التخطيط. ورموزُ
      المصحف في منطقة الاستعمال الخاصّ من المستوى الأساسيّ، فكلُّ
      رمزٍ محرفٌ واحد ودليلُه هو دليلُ الكلمة نفسِه.

   والورقةُ من مصحفٍ مجلَّد: هامشُها الخارجيُّ أوسعُ من الداخليّ،
   والكعبُ يُلقي ظلاًّ. والفرديّةُ في الكتاب العربيّ يسارَ الفتحة
   فكعبُها عن يمينها.
══════════════════════════════════════════════════════════════ */

/** صندوقُ السطر بالـem — مقيسٌ من `hhea` في الخطّ لا مُقدَّر. */
private const val LINE_BOX = 2.28f

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
    /** نقرةٌ قصيرة — تُظهر الأدواتِ وتُخفيها. */
    onTap: () -> Unit,
    /** ضغطةٌ مطوّلة — تفتح الآية. */
    onVerseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val starts = remember(page) {
        var n = 0
        page.l.map { row -> n.also { n += row.size } }
    }
    val lineTypes = remember(page) { page.l.indices.map { page.typesOf(it) } }

    // أعرضُ سطرِ متنٍ — واللوحُ والبسملةُ خارجَه لأنّ em خطِّهما آخر.
    val widestEm = remember(page) {
        var w = 0
        page.l.indices.forEach { li ->
            val t = page.typesOf(li)
            if (GlyphType.SURAH_HEADER !in t && GlyphType.BISMILLAH !in t) {
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
            val availW = maxWidth.value
            val availH = maxHeight.value
            val lines = page.l.size.coerceAtLeast(1)

            /*  أصغرُ المشتقَّين: ما يملأ العرضَ وما يسع الارتفاع.
                و0.985 نفَسٌ بين الأسطر يوزّعه `SpaceEvenly`، ووقايةٌ من
                كسرِ التقريب في قياس النصّ. */
            val fs = minOf(availW / widestEm, availH * 0.985f / (lines * LINE_BOX))

            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                page.l.forEachIndexed { li, row ->
                    val types = lineTypes[li]
                    val plate = GlyphType.SURAH_HEADER in types
                    val bism = GlyphType.BISMILLAH in types
                    MushafLine(
                        page = page,
                        line = li,
                        start = starts[li],
                        size = row.size,
                        family = when {
                            plate -> fonts.plate ?: fonts.body
                            bism -> fonts.bism ?: fonts.body
                            else -> fonts.body
                        },
                        fontSize = when {
                            plate -> availW * PLATE_FILL / (page.lw.getOrNull(li) ?: 330).let { it / 100f }
                            bism -> availW * BISM_FILL / (page.lw.getOrNull(li) ?: 800).let { it / 100f }
                            else -> fs
                        },
                        availWidth = availW,
                        decorated = plate || bism,
                        ink = ink,
                        accent = accent,
                        marker = marker,
                        selectedVerse = selectedVerse,
                        onTap = onTap,
                        onVerseClick = onVerseClick,
                    )
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

@Composable
private fun MushafLine(
    page: MushafPage,
    line: Int,
    start: Int,
    size: Int,
    family: FontFamily,
    fontSize: Float,
    availWidth: Float,
    decorated: Boolean,
    ink: Color,
    accent: Color,
    marker: Color,
    selectedVerse: String?,
    onTap: () -> Unit,
    onVerseClick: (String) -> Unit,
) {
    val text: AnnotatedString = remember(page, line, selectedVerse, ink, accent, marker) {
        buildAnnotatedString {
            page.l[line].forEachIndexed { i, delta ->
                val g = start + i
                val type = page.t.getOrNull(g) ?: GlyphType.WORD
                val vk = page.v.getOrNull(g).orEmpty()
                val picked = vk.isNotEmpty() && vk == selectedVerse
                withStyle(
                    SpanStyle(
                        color = when {
                            type == GlyphType.AYAH_END || type == GlyphType.QUARTER -> marker
                            type == GlyphType.SURAH_HEADER || type == GlyphType.BISMILLAH -> accent
                            picked -> accent
                            else -> ink
                        },
                        background = if (picked) marker.copy(alpha = 0.15f) else Color.Unspecified,
                    ),
                ) { append(page.glyph(delta)) }
            }
        }
    }

    val naturalEm = (page.lw.getOrNull(line) ?: 1630) / 100f
    val natural = naturalEm * fontSize
    val fills = !decorated && natural >= availWidth * FULL_LINE

    /*  الفاضلُ يُوزَّع بين الرموز — وكلُّ رمزٍ كلمة، فهو توسيعُ ما بين
        الكلمات. والـ0.998 هامشُ أمانٍ من كسرِ التقريب. */
    val spacing = if (fills && size > 0) {
        ((availWidth * 0.998f - natural) / fontSize / size).coerceAtLeast(0f)
    } else {
        0f
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = text,
        fontFamily = family,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * LINE_BOX).sp,
        letterSpacing = spacing.em,
        textAlign = if (fills) TextAlign.Start else TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { layout = it },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(page, line) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { pos ->
                        /*  رموزُ المصحف من المستوى الأساسيّ، فكلُّ رمزٍ
                            محرفٌ واحد ودليلُه دليلُ الكلمة. */
                        val l = layout ?: return@detectTapGestures
                        val i = l.getOffsetForPosition(pos).coerceIn(0, (size - 1).coerceAtLeast(0))
                        page.v.getOrNull(start + i)
                            ?.takeIf { it.isNotEmpty() }
                            ?.let(onVerseClick)
                    },
                )
            },
    )
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
