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
        /* رأس الصفحة: الاسم يميناً، ومخرجٌ يساراً */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            if (page == 0 || last) {
                Column {
                    Text(stringResource(R.string.ob_brand), style = RafiqType.titleM, color = rc.ink)
                    Text(stringResource(R.string.ob_brand_sub), style = RafiqType.bodyS, color = rc.inkMed)
                }
            } else {
                Text(stringResource(R.string.ob_brand), style = RafiqType.bodyS, color = rc.inkMed)
            }
            if (!last) {
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

        /* المسرح: القطعةُ الحيّة — أو فراغٌ يدفع الكلام إلى أسفل */
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            when (page) {
                1 -> Stage(stringResource(R.string.ob2_hint)) { SanadCard(rc) }
                2 -> Stage(stringResource(R.string.ob3_hint)) { WeekPreview(rc) }
            }
        }

        if (page == 1 || page == 2) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
            Spacer(Modifier.height(18.dp))
        }

        Text(
            stringResource(
                when (page) {
                    0 -> R.string.ob1_title; 1 -> R.string.ob2_title
                    2 -> R.string.ob3_title; else -> R.string.ob4_title
                },
            ),
            style = RafiqType.ayah,
            color = rc.ink,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(
                when (page) {
                    0 -> R.string.ob1_body; 1 -> R.string.ob2_body
                    2 -> R.string.ob3_body; else -> R.string.ob4_body
                },
            ),
            style = RafiqType.body,
            color = rc.inkMed,
        )

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

@Composable
private fun Stage(hint: String, content: @Composable () -> Unit) {
    val rc = LocalRafiqColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(hint, style = RafiqType.bodyS, color = rc.inkMed)
        Spacer(Modifier.height(14.dp))
        content()
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
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.cardBorder, RafiqShape.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Spacer(Modifier.width(52.dp))
            PREVIEW_DAYS.forEach {
                Text(
                    it,
                    style     = RafiqType.caption,
                    color     = rc.inkMed,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
            }
        }
        PREVIEW_ROWS.forEach { (label, cells) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(label, style = RafiqType.caption, color = rc.inkMed, maxLines = 1,
                    modifier = Modifier.width(52.dp))
                cells.forEach { v ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (v == 1) on else off),
                    )
                }
            }
        }
    }
}

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
