package app.rafiqaldhikr.ui.screens.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.IcoBookOpen
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.mushaf.MushafFonts
import app.rafiqaldhikr.ui.mushaf.MushafLayout
import app.rafiqaldhikr.ui.mushaf.MushafPage
import app.rafiqaldhikr.ui.mushaf.MushafPageView
import app.rafiqaldhikr.ui.mushaf.PageFonts
import app.rafiqaldhikr.ui.mushaf.SurahNames
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* ══════════════════════════════════════════════════════════════
   تابِع القراءة — **بصفحتك أنت**

   كانت سطرين بجانب أيقونة كتاب: «تابِع القراءة · البقرة صفحة ١١».
   وأيقونةُ الكتاب تصلح لأيّ تطبيقٍ في الدنيا، ولا تقول أيَّ صفحةٍ
   تركتَ.

   والصفحةُ هنا **مرسومةٌ من بيانات التطبيق نفسِه**: تخطيطُها من
   `mushaf_layout.json` وحروفُها من خطّ `QCF4_Hafs` المشحون في الحزمة —
   وهو `MushafPageView` عينُه الذي يُقرأ منه المصحف، لا نسخةٌ ثانية.
   فما تراه هو صفحتُك بعينها مهما تغيّرت، لا صورةٌ ثابتةٌ تكذّب السطرَ
   المكتوب بجانبها. ولا ملفَّ صورةٍ يُضاف، ولا مسألةَ ترخيص.

   ═══ ثلاثةُ أشياءَ في التنفيذ ═══

   **التخطيطُ يُقرأ خارج الخيط الرئيسيّ.** ملفُّه ١٫٢ ميغابايت، وتحليلُه
   داخل التأليف يوقف أوّلَ إطارٍ في الشاشة الأولى. فيُقرأ في الخلفيّة،
   وتبقى البطاقةُ بأيقونتها حتى يجهز ثمّ تنقلب صفحةً.

   **والصفحةُ لا يقرؤها التدقيقُ الصوتيّ.** رموزُها من منطقة الاستعمال
   الخاصّ، فقراءتُها حرفاً حرفاً لغوٌ لا قرآن. تُمسَح دلالتُها بـ
   `clearAndSetSemantics`، ويحمل المعنى سطرُ البطاقة المكتوب.

   **واللمسةُ للبطاقة كلِّها.** أسطرُ `MushafPageView` قابلةٌ للنقر في
   المصحف (تفتح الآية)، ولو تُركت هنا لسرقت لمسةَ البطاقة. فطبقةٌ
   شفّافةٌ فوق الكلّ تلتقطها — وهي **آخرُ ما يُرسم**، والأعلى يفوز.
══════════════════════════════════════════════════════════════ */

private val CARD_H = 96.dp
private val SHOT_W = 150.dp
/*  الصفحةُ تُخطَّط بعرضٍ كامل ثمّ تُصغَّر، لا تُخطَّط صغيرةً: مقاسُ
 *  الخطّ في `MushafPageView` مشتقٌّ من العرض، فلو خُطّطت بـ١٥٠ لخرجت
 *  بأسطرٍ عريضةٍ ركيكة. ٢٧٠ × ٠٫٥٦ = ١٥١ — تملأ الشريط بالضبط. */
private val PAGE_W = 270.dp
private val PAGE_H = 420.dp
private const val SHOT_SCALE = 0.56f

/** صفحةٌ جاهزةٌ للعرض: تخطيطُها وخطوطُها ومدى آياتها. */
private class Shot(val page: MushafPage, val fonts: PageFonts, val from: Int, val to: Int)

@Composable
fun ContinueReading(surah: Int, page: Int, ar: Boolean, onOpen: () -> Unit) {
    val rc = LocalRafiqColors.current
    val ctx = LocalContext.current
    val name = remember(surah) { SurahNames.of(ctx, surah) }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var shot by remember(page, surah) { mutableStateOf<Shot?>(null) }
    LaunchedEffect(page, surah) {
        shot = withContext(Dispatchers.IO) {
            runCatching {
                val layout = MushafLayout.load(ctx)
                val pg = layout.page(page) ?: return@runCatching null
                val fonts = MushafFonts(ctx).pageFonts(layout, page) ?: return@runCatching null
                //  مدى الآيات: من مفاتيح الصفحة نفسِها، مقصورةً على سورتها
                val nums = pg.v.mapNotNull { k ->
                    val s = k.substringBefore(':').toIntOrNull()
                    if (s == surah) k.substringAfter(':').toIntOrNull() else null
                }
                Shot(pg, fonts, nums.minOrNull() ?: 0, nums.maxOrNull() ?: 0)
            }.getOrNull()
        }
    }

    val label = stringResource(R.string.continue_reading) + " · " +
        stringResource(R.string.continue_reading_at, name, page.toString())

    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .height(CARD_H)
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.card),
    ) {
        val s = shot
        if (s != null) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .width(SHOT_W)
                    .fillMaxHeight()
                    .clipToBounds()
                    .clearAndSetSemantics { },
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = (-6).dp)
                        .requiredSize(PAGE_W, PAGE_H)
                        .graphicsLayer {
                            scaleX = SHOT_SCALE
                            scaleY = SHOT_SCALE
                            transformOrigin = TransformOrigin(if (rtl) 0f else 1f, 0f)
                        },
                ) {
                    MushafPageView(
                        page = s.page,
                        fonts = s.fonts,
                        ink = rc.ink,
                        accent = rc.gold,
                        marker = rc.emerald,
                        selectedVerse = null,
                        onTap = {},
                        onVerseClick = {},
                    )
                }
                /*  الحجاب: الصفحةُ **أوضحُ ما تكون عند الكلام** وتذوب
                 *  في لون البطاقة عند حافّتها الخارجيّة — فتُقرأ كأنّها
                 *  خلف البطاقة تُطلّ من نافذة، لا صورةً مُلصَقة.
                 *
                 *  ولا لونَ ورقٍ خلفها: لونُ البطاقة نفسُه. وأيُّ لونٍ
                 *  ثانٍ يصنع حدّاً رأسيّاً حادّاً عند طرف الشريط
                 *  الداخليّ — وهو أوّلُ ما تقع عليه العين. */
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                if (rtl) {
                                    listOf(rc.card, rc.card.copy(alpha = 0f))
                                } else {
                                    listOf(rc.card.copy(alpha = 0f), rc.card)
                                },
                            ),
                        ),
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = if (s != null) SHOT_W - 6.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (s == null) {
                IcoBookOpen(20.dp, rc.emerald)
                Spacer(Modifier.width(12.dp))
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    stringResource(R.string.continue_reading),
                    style = RafiqType.bodyS,
                    color = rc.inkMed,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.continue_reading_at, name, page.toString())
                        .localizedDigits(ar),
                    style = RafiqType.body,
                    color = rc.ink,
                )
                if (s != null && s.to > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(
                            R.string.continue_reading_ayat,
                            s.from.toString(), s.to.toString(),
                        ).localizedDigits(ar),
                        style = RafiqType.bodyS,
                        color = rc.inkMed,
                    )
                }
            }
        }

        if (s != null) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = SHOT_W - 44.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(rc.card.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) { RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight) }
        } else {
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
            }
        }

        /*  اللمسةُ آخرَ طبقةٍ فتفوز على أسطر الصفحة. */
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable(onClick = onOpen)
                .semantics {
                    contentDescription = label
                    onClick(label = null) { onOpen(); true }
                },
        )
    }
}
