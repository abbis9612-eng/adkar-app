package app.rafiqaldhikr.ui.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.util.rememberPermissionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/* ══════════════════════════════════════════════════════════════
   شاشة الترحيب — أربعُ شاشات

   البنية في كلٍّ منها واحدة: [قطعةٌ حيّة] ← فاصل ← [عنوان ووصف] ← [زرّ].
   القطعة الحيّة هي الشيءُ نفسه لا رسمٌ يمثّله: بطاقةُ ذكرٍ بتخريجها،
   وشبكةُ أسبوعٍ من «أوراقي». والكلامُ كلُّه أسفل حيث يقع الإبهام.

   والكلماتُ دعوةٌ لا إعلان: لا «٨٧ نصّاً» ولا «يعمل دون إنترنت» —
   فلا أحد يفتح تطبيق ذكرٍ ليقتنع بمواصفاته.

   حُذفت الشاشةُ الثانية القديمة («يومك كلُّه في ورقةٍ واحدة»): كانت
   تشرح ميزةً تشرحها الشاشةُ الثالثة بعرضها.
══════════════════════════════════════════════════════════════ */

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    settingsVM:    SettingsViewModel = koinViewModel(),
) {
    val rc          = LocalRafiqColors.current
    val pagerState  = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope       = rememberCoroutineScope()
    val permissions = rememberPermissionState()

    /**
     * @param askNotifications زرُّ الشاشة الأخيرة يطلب الإذن، و«أذكّرني
     *   لاحقاً» و«تخطّي» لا يطلبانه — وإلّا كان الرابطان كذباً.
     */
    fun finish(askNotifications: Boolean) {
        if (askNotifications && !permissions.hasNotificationPermission()) {
            permissions.requestNotificationPermission()
        }
        settingsVM.completeOnboarding()
        navController.navigate(RafiqRoute.Home.route) {
            popUpTo(RafiqRoute.Onboarding.route) { inclusive = true }
        }
    }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        MeshBackdrop(rc)
        HorizontalPager(
            state         = pagerState,
            modifier      = Modifier.fillMaxSize(),
            reverseLayout = true,           // العربية تُقلَب، والصفحة الأولى يمين
        ) { page ->
            OnboardingPage(
                page    = page,
                onNext  = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                onStart = { finish(askNotifications = true) },
                onLater = { finish(askNotifications = false) },
            )
        }
    }
}

/* ── الخلفية: شبكةُ هالاتٍ فاتحة ───────────────────────────────

   أربعُ هالاتٍ شعاعية تتداخل، مشتقّةٌ كلُّها من لوحة المستخدم لا من
   ألوانٍ مثبَّتة — فتتبع الشبكةُ لونَه إذا غيّره من شاشة الألوان،
   وتنقلب مع الوضع الداكن بلا شرط.
──────────────────────────────────────────────────────────────── */

@Composable
private fun MeshBackdrop(rc: RafiqPalette) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        fun halo(color: Color, cx: Float, cy: Float, r: Float) {
            drawCircle(
                Brush.radialGradient(
                    listOf(color, color.copy(alpha = 0f)),
                    center = Offset(cx, cy),
                    radius = r,
                ),
                radius = r,
                center = Offset(cx, cy),
            )
        }
        halo(lerp(rc.bg, rc.emeraldFill, 0.55f), w * 0.06f, h * 0.02f, w * 0.78f)
        halo(lerp(rc.bg, rc.emeraldFill, 0.42f), w * 0.98f, h * 0.16f, w * 0.62f)
        halo(lerp(rc.bg, rc.emerald,     0.16f), w * 0.72f, h * 0.34f, w * 0.60f)
        halo(lerp(rc.bg, rc.card,        0.90f), w * 0.50f, h * 1.02f, w * 0.95f)
    }
}

/* ── صفحةٌ واحدة ──────────────────────────────────────────────── */

@Composable
private fun OnboardingPage(
    page:    Int,
    onNext:  () -> Unit,
    onStart: () -> Unit,
    onLater: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val last = page == PAGE_COUNT - 1

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        /* رأس الصفحة: الاسم يميناً، ومخرجٌ يساراً.
           لا رأسَ في الأخيرة — لا اسمَ يُعرَّف بعد ثلاثِ شاشات، ولا مخرجَ
           والزرُّ نفسه هو المخرج. وفراغُه يذهب إلى الخاتَم. */
        if (!last) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                if (page == 0) {
                    Column {
                        Text(stringResource(R.string.ob_brand), style = RafiqType.titleM, color = rc.ink)
                        Text(stringResource(R.string.ob_brand_sub), style = RafiqType.bodyS, color = rc.inkMed)
                    }
                } else {
                    Text(stringResource(R.string.ob_brand), style = RafiqType.bodyS, color = rc.inkMed)
                }
                Text(
                    stringResource(R.string.ob_skip),
                    style    = RafiqType.bodyS,
                    color    = rc.inkMed,
                    modifier = Modifier
                        .clip(RafiqShape.chip)
                        .clickable(onClick = onLater)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        /* المسرح: لكلِّ شاشةٍ قطعتُها — ولا شاشةَ فارغة.
           كانت الأولى والأخيرة بلا قطعة، فبقي نصفُ الشاشة بياضاً في كلٍّ
           منهما، وتشابهتا حتى قُرِئتا مكرَّرتين. */
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            when (page) {
                0    -> ArcOfDay(rc)
                1    -> SanadCard(rc)
                2    -> WeekPreview(rc)
                else -> Seal(rc)
            }
        }

        if (page == 1 || page == 2) {
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
            Spacer(Modifier.height(18.dp))
        } else {
            Spacer(Modifier.height(18.dp))
        }

        // الأخيرةُ توسَّط والباقياتُ تبدأ من الحافّة — فرقُ محورٍ يُرى قبل
        // أن يُقرأ، وهو ما يفصل الخاتمةَ عن الدعوة.
        val align = if (last) Alignment.CenterHorizontally else Alignment.Start
        val textAlign = if (last) TextAlign.Center else TextAlign.Start

        Column(Modifier.fillMaxWidth(), horizontalAlignment = align) {
            Text(
                stringResource(
                    when (page) {
                        0 -> R.string.ob1_title; 1 -> R.string.ob2_title
                        2 -> R.string.ob3_title; else -> R.string.ob4_title
                    },
                ),
                style     = RafiqType.hero,
                color     = rc.ink,
                textAlign = textAlign,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(
                    when (page) {
                        0 -> R.string.ob1_body; 1 -> R.string.ob2_body
                        2 -> R.string.ob3_body; else -> R.string.ob4_body
                    },
                ),
                style     = RafiqType.bodyL,
                color     = rc.inkMed,
                textAlign = textAlign,
            )
        }

        Spacer(Modifier.height(20.dp))
        Rosettes(current = page, rc = rc)
        Spacer(Modifier.height(16.dp))

        /* الزرّ: حبرٌ صلبٌ لا تدرّج — الشبكةُ خلفه هي اللون، فزرٌّ ملوّن
           فوقها يصير لهجةً ثانية تنافسها. */
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .clip(RafiqShape.chip)
                .background(rc.ink)
                .clickable { if (last) onStart() else onNext() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(
                    when (page) {
                        0 -> R.string.ob1_cta
                        PAGE_COUNT - 1 -> R.string.ob4_cta
                        else -> R.string.ob_next
                    },
                ),
                style = RafiqType.titleM,
                color = rc.bg,
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            if (last) stringResource(R.string.ob4_link) else "",
            style     = RafiqType.bodyS,
            color     = rc.inkMed,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .fillMaxWidth()
                .heightIn(min = 22.dp)
                .then(if (last) Modifier.clickable(onClick = onLater) else Modifier),
        )
    }
}

/* ── قطعةُ الشاشة الأولى: قوسُ اليوم ────────────────────────────

   ثمانيةُ مراسٍ على قوسٍ واحد — هي محطّاتُ «رفيق اليوم» نفسها. والقوسُ
   يتدرّج من ليلٍ إلى فجرٍ إلى نهارٍ إلى غسقٍ إلى ليل، بألوان الضوء
   الموجودة في اللوحة أصلاً (lightNight وlightDusk)، فيرسم العنوانَ
   الذي تحته: «من استيقاظك إلى نومك».
──────────────────────────────────────────────────────────────── */

@Composable
private fun ArcOfDay(rc: RafiqPalette) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(132.dp),
        ) {
            val w  = size.width
            val h  = size.height
            val y1 = h * 0.86f                   // طرفا القوس
            val cy = -h * 0.28f                  // نقطةُ تحكّمٍ فوق الإطار: قمّةٌ ضحلة
            val x0 = w * 0.06f
            val x2 = w * 0.94f
            val cx = w * 0.50f

            fun at(tt: Float): Offset {
                val u = 1f - tt
                return Offset(
                    u * u * x0 + 2f * u * tt * cx + tt * tt * x2,
                    u * u * y1 + 2f * u * tt * cy + tt * tt * y1,
                )
            }

            val path = Path().apply {
                moveTo(x0, y1)
                quadraticBezierTo(cx, cy, x2, y1)
            }
            drawPath(
                path,
                Brush.horizontalGradient(
                    listOf(rc.lightNight, rc.lightDusk, rc.emeraldFill, rc.lightDusk, rc.lightNight),
                ),
                style = Stroke(w * 0.006f),
            )

            // ثمانيةُ مراسٍ موزَّعةٌ على القوس — أوّلُها وآخرُها أكبر
            for (i in 0 until 8) {
                val tt = i / 7f
                val pt = at(tt)
                val edge = i == 0 || i == 7
                drawCircle(rc.bg, w * (if (edge) 0.030f else 0.020f), pt)
                drawCircle(
                    if (edge) rc.emerald else rc.emeraldFill,
                    w * (if (edge) 0.022f else 0.013f),
                    pt,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.ob_arc_start), style = RafiqType.caption, color = rc.inkMed)
            Text(stringResource(R.string.ob_arc_end),   style = RafiqType.caption, color = rc.inkMed)
        }
    }
}

/* ── قطعةُ الشاشة الأخيرة: الخاتَم ──────────────────────────────

   الوريدةُ نفسها التي تُعلّم الصفحات، مكبَّرةً وحدها في الوسط. لا تشرح
   شيئاً — وهذه وظيفتُها: الشاشةُ الأخيرة خاتمةٌ لا صفحةُ بيع.
──────────────────────────────────────────────────────────────── */

@Composable
private fun Seal(rc: RafiqPalette) {
    Canvas(Modifier.size(168.dp)) {
        val w = size.width
        val c = Offset(w / 2f, w / 2f)
        drawCircle(
            Brush.radialGradient(
                listOf(rc.emeraldFill.copy(alpha = 0.22f), rc.emeraldFill.copy(alpha = 0f)),
                center = c,
                radius = w * 0.52f,
            ),
            radius = w * 0.52f,
            center = c,
        )
        drawCircle(rc.emerald.copy(alpha = 0.28f), w * 0.46f, c, style = Stroke(w * 0.006f))
        drawRosette(filled = true, color = rc.emerald)
    }
}

/* ── القطعة الحيّة ١: ذكرٌ بتخريجه ─────────────────────────────

   نصُّ الذكر وتخريجُه يظهران معاً على الشاشة نفسها — وهو شرطُ عرض أيّ
   نصٍّ شرعيّ في التطبيق، لا زينةَ تصميم. راجع
   tools/check_devotional_in_code.py.
──────────────────────────────────────────────────────────────── */

@Composable
private fun SanadCard(rc: RafiqPalette) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.cardBorder, RafiqShape.card)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.ob2_dhikr),
            style     = RafiqType.dhikr,
            color     = rc.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(rc.emeraldFill),
            )
            Spacer(Modifier.width(9.dp))
            Column {
                Text(stringResource(R.string.ob2_dhikr_src), style = RafiqType.label, color = rc.emerald)
                Text(stringResource(R.string.ob2_dhikr_grade), style = RafiqType.caption, color = rc.inkMed)
            }
        }
    }
}

/* ── القطعة الحيّة ٢: شبكةُ أسبوعٍ من «أوراقي» ───────────────────

   نمطٌ ثابت للعرض — لا بيانات للمستخدم بعد وهو في شاشة الترحيب.
──────────────────────────────────────────────────────────────── */

// خمسةُ صفوفٍ لا ثمانية: البطاقةُ هنا لمحةٌ في مساحةٍ مرنة، وثمانيةٌ
// تُقصّ على الشاشات القصيرة. والشبكةُ كاملةً في «أوراقي».
private val PREVIEW_ROWS = listOf(
    "الفجر"   to listOf(1, 1, 1, 1, 1, 1, 0),
    "الضحى"   to listOf(1, 0, 1, 0, 1, 1, 0),
    "الظهر"   to listOf(1, 1, 1, 1, 0, 1, 0),
    "المغرب"  to listOf(1, 1, 1, 1, 1, 0, 0),
    "النوم"   to listOf(0, 1, 1, 0, 1, 1, 0),
)
private val PREVIEW_DAYS = listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")

@Composable
private fun WeekPreview(rc: RafiqPalette) {
    val on  = lerp(rc.emeraldFill, rc.ink, 0.10f)
    val off = lerp(rc.bg, rc.ink, 0.12f)
    // البطاقةُ تقيس المساحةَ المتاحة وتشتقّ منها ضلعَ الخانة. بلا هذا كان
    // ضلعُها من العرض وحده (aspectRatio)، فتُقصّ صفوفُها على شاشةٍ قصيرة.
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val rows    = PREVIEW_ROWS.size
        val chrome  = CARD_PAD * 2 + HEAD_H + GAP
        val byH     = ((maxHeight - chrome - GAP * (rows - 1)) / rows).coerceAtLeast(9.dp)
        val byW     = ((maxWidth - CARD_PAD * 2 - LABEL_W - GAP * 6) / 7).coerceAtLeast(9.dp)
        val cell    = minOf(byH, byW)

        Column(
            Modifier
                .width(CARD_PAD * 2 + LABEL_W + GAP * 6 + cell * 7)
                .clip(RafiqShape.card)
                .background(rc.card)
                .border(1.dp, rc.cardBorder, RafiqShape.card)
                .padding(CARD_PAD),
            verticalArrangement = Arrangement.spacedBy(GAP),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                Spacer(Modifier.width(LABEL_W))
                PREVIEW_DAYS.forEach {
                    Text(
                        it,
                        style     = RafiqType.caption,
                        color     = rc.inkMed,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.width(cell),
                    )
                }
            }
            PREVIEW_ROWS.forEach { (label, cells) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GAP),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        label,
                        style    = RafiqType.caption,
                        color    = rc.inkMed,
                        maxLines = 1,
                        modifier = Modifier.width(LABEL_W),
                    )
                    cells.forEach { v ->
                        Box(
                            Modifier
                                .size(cell)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (v == 1) on else off),
                        )
                    }
                }
            }
        }
    }
}

private val CARD_PAD = 16.dp
private val LABEL_W  = 52.dp
private val HEAD_H   = 20.dp
private val GAP      = 4.dp

/* ── مؤشّر الصفحات: وريداتٌ لا نقاط ────────────────────────────

   أربعُ إهليلجاتٍ متقاطعة — الوريدةُ الإسلامية. والنقطةُ المستديرة
   عامّة، وهذه من زخرفة الورق نفسه.
──────────────────────────────────────────────────────────────── */

@Composable
private fun Rosettes(current: Int, rc: RafiqPalette) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(PAGE_COUNT) { i ->
            Canvas(Modifier.size(17.dp)) {
                drawRosette(filled = i == current, color = if (i == current) rc.emerald else rc.inkLight)
            }
            if (i < PAGE_COUNT - 1) Spacer(Modifier.width(11.dp))
        }
    }
}

private fun DrawScope.drawRosette(filled: Boolean, color: Color) {
    val w  = size.width
    val c  = Offset(w / 2f, w / 2f)
    val rx = w * 0.40f
    val ry = w * 0.17f
    listOf(0f, 45f, 90f, 135f).forEach { a ->
        rotate(a, c) {
            val topLeft = Offset(c.x - rx, c.y - ry)
            val sz      = Size(rx * 2, ry * 2)
            if (filled) drawOval(color.copy(alpha = 0.35f), topLeft, sz)
            drawOval(color, topLeft, sz, style = Stroke(w * 0.05f))
        }
    }
    drawCircle(color, w * 0.10f, c)
}
