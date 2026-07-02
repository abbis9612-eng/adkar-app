package app.rafiqaldhikr.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.theme.RafiqPalette
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.util.rememberPermissionState
import kotlin.math.*

/* ══════════════════════════════════════════════════════════════
   DESIGN TOKENS
══════════════════════════════════════════════════════════════ */

/* Colors provided by LocalRafiqColors */

/* ══════════════════════════════════════════════════════════════
   DATA
══════════════════════════════════════════════════════════════ */

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconType: Int,        // 0=welcome, 1=adhkar, 2=quran, 3=start
)

private val PAGES = listOf(
    OnboardingPageData(
        title       = "رفيق الذّكر",
        subtitle    = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        description = "رفيقك اليومي في رحلة الإيمان\nوالتقرب إلى الله عز وجل",
        iconType    = 0,
    ),
    OnboardingPageData(
        title       = "أذكارك اليومية",
        subtitle    = "أذكار الصباح والمساء",
        description = "لا تفوّت أذكارك بتذكيرات ذكية\nفي الأوقات المناسبة لكل ذكر",
        iconType    = 1,
    ),
    OnboardingPageData(
        title       = "القرآن والتسبيح",
        subtitle    = "تلاوة · تسبيح · متابعة",
        description = "اقرأ القرآن واستمع لأفضل القراء\nوسبّح بالمسبحة الرقمية مع التتبع",
        iconType    = 2,
    ),
    OnboardingPageData(
        title       = "مستعد للبدء؟",
        subtitle    = "خطوة واحدة",
        description = "فعّل الإشعارات ليصلك تذكير\nبأذكارك ومواقيت صلاتك",
        iconType    = 3,
    ),
)

/* ══════════════════════════════════════════════════════════════
   MAIN COMPOSABLE
══════════════════════════════════════════════════════════════ */

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    settingsVM:    SettingsViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val pagerState  = rememberPagerState(pageCount = { PAGES.size })
    val scope       = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val permissions = rememberPermissionState()

    fun finishOnboarding() {
        // نطلب إذن الإشعارات هنا؛ إذن الموقع يُطلب تلقائياً في الرئيسية
        if (!permissions.hasNotificationPermission()) {
            permissions.requestNotificationPermission()
        }
        settingsVM.completeOnboarding()
        navController.navigate(RafiqRoute.Home.route) {
            popUpTo(RafiqRoute.Onboarding.route) { inclusive = true }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
    ) {
        // ═══ PAGER ═══
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true, // RTL support
        ) { page ->
            OnboardingPage(data = PAGES[page], pageIndex = page)
        }

        // ═══ BOTTOM CONTROLS ═══
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dots
            PageDots(total = PAGES.size, current = currentPage)

            Spacer(Modifier.height(28.dp))

            // Main button
            val isLast = currentPage == PAGES.lastIndex
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isLast) listOf(rc.gold, rc.goldLight)
                            else listOf(rc.emerald, rc.emeraldMed)
                        )
                    )
                    .clickable {
                        if (isLast) {
                            finishOnboarding()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (isLast) "ابدأ رحلتك" else "التالي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalRafiqColors.current.card,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Skip
            if (!isLast) {
                Text(
                    "تخطي",
                    fontSize = 14.sp,
                    color = LocalRafiqColors.current.inkMed,
                    modifier = Modifier.clickable { finishOnboarding() }
                )
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   SINGLE PAGE
══════════════════════════════════════════════════════════════ */

@Composable
private fun OnboardingPage(data: OnboardingPageData, pageIndex: Int) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 36.dp, bottom = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Canvas art
        Box(
            Modifier
                .size(260.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            OnboardingArt(iconType = data.iconType)
        }

        Spacer(Modifier.height(36.dp))

        // Subtitle (basmala / category)
        Text(
            data.subtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalRafiqColors.current.gold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(10.dp))

        // Title
        Text(
            data.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = LocalRafiqColors.current.ink,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(14.dp))

        // Description
        Text(
            data.description,
            fontSize = 16.sp,
            color = LocalRafiqColors.current.inkMed,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
        )
    }
}

/* ══════════════════════════════════════════════════════════════
   PAGE DOTS
══════════════════════════════════════════════════════════════ */

@Composable
private fun PageDots(total: Int, current: Int) {
    val rc = LocalRafiqColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { idx ->
            val isActive = idx == current
            val width by animateDpAsState(
                if (isActive) 28.dp else 8.dp,
                tween(300), label = "dotW"
            )
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive) rc.emerald else rc.inkLight.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   CANVAS ART — One unique illustration per page
══════════════════════════════════════════════════════════════ */

@Composable
private fun OnboardingArt(iconType: Int) {
    val rc = LocalRafiqColors.current
    val inf = rememberInfiniteTransition(label = "onbArt")

    val rotation by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "rot"
    )

    val pulse by inf.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val shimmer by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shimmer"
    )

    Canvas(Modifier.size(260.dp)) {
        val w = size.width
        val cx = w / 2f
        val cy = w / 2f

        // Background circle (soft green)
        drawCircle(
            Brush.radialGradient(
                listOf(rc.emeraldPastel, rc.emeraldPastel.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.50f,
            ),
            radius = w * 0.50f,
            center = Offset(cx, cy),
        )

        // Rotating outer ring
        rotate(rotation, Offset(cx, cy)) {
            val ringStroke = Stroke(w * 0.008f)
            drawCircle(rc.gold.copy(alpha = 0.18f), w * 0.46f, Offset(cx, cy), style = ringStroke)

            // Gold dots around the ring
            for (i in 0 until 12) {
                val a = (i * 30) * PI.toFloat() / 180f
                val dx = cx + w * 0.46f * cos(a)
                val dy = cy + w * 0.46f * sin(a)
                drawCircle(rc.gold.copy(alpha = 0.25f), w * 0.012f, Offset(dx, dy))
            }
        }

        // Inner decorative ring (counter-rotate)
        rotate(-rotation * 0.5f, Offset(cx, cy)) {
            val ringStroke2 = Stroke(w * 0.005f)
            drawCircle(rc.emerald.copy(alpha = 0.10f), w * 0.38f, Offset(cx, cy), style = ringStroke2)

            for (i in 0 until 8) {
                val a = (i * 45) * PI.toFloat() / 180f
                val dx = cx + w * 0.38f * cos(a)
                val dy = cy + w * 0.38f * sin(a)
                val dSize = w * 0.025f
                rotate((i * 45).toFloat(), Offset(dx, dy)) {
                    drawRect(
                        rc.emerald.copy(alpha = 0.10f),
                        Offset(dx - dSize / 2, dy - dSize / 2),
                        Size(dSize, dSize),
                        style = Stroke(w * 0.004f)
                    )
                }
            }
        }

        // Scale for pulse
        scale(pulse, Offset(cx, cy)) {
            when (iconType) {
                0 -> drawWelcomeIcon(cx, cy, w, shimmer, rc)
                1 -> drawAdhkarIcon(cx, cy, w, shimmer, rc)
                2 -> drawQuranIcon(cx, cy, w, shimmer, rc)
                3 -> drawStartIcon(cx, cy, w, shimmer, rc)
            }
        }
    }
}

/* ── Page 0: Welcome — Mosque dome + crescent ── */

private fun DrawScope.drawWelcomeIcon(cx: Float, cy: Float, w: Float, shimmer: Float, rc: RafiqPalette) {
    val goldShimmer = rc.gold.copy(alpha = 0.6f + shimmer * 0.4f)

    // Dome
    val domeTop = cy - w * 0.14f
    val domeW = w * 0.30f
    val domeH = w * 0.18f
    drawArc(
        rc.emerald,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - domeW / 2, domeTop - domeH / 2),
        size = Size(domeW, domeH),
    )

    // Body of mosque
    drawRoundRect(
        rc.emerald,
        Offset(cx - w * 0.18f, domeTop + domeH * 0.35f),
        Size(w * 0.36f, w * 0.18f),
        cornerRadius = CornerRadius(w * 0.02f),
    )

    // Minarets
    val minaretW = w * 0.035f
    val minaretH = w * 0.22f
    // Left
    drawRoundRect(
        rc.emeraldMed,
        Offset(cx - w * 0.22f, cy - w * 0.08f - minaretH),
        Size(minaretW, minaretH),
        cornerRadius = CornerRadius(minaretW / 2),
    )
    // Right
    drawRoundRect(
        rc.emeraldMed,
        Offset(cx + w * 0.22f - minaretW, cy - w * 0.08f - minaretH),
        Size(minaretW, minaretH),
        cornerRadius = CornerRadius(minaretW / 2),
    )

    // Crescent on dome
    val crescentR = w * 0.04f
    drawCircle(goldShimmer, crescentR, Offset(cx, domeTop - domeH * 0.18f))
    drawCircle(rc.emerald, crescentR * 0.7f, Offset(cx + crescentR * 0.35f, domeTop - domeH * 0.18f - crescentR * 0.1f))

    // Base line
    drawLine(
        rc.emerald.copy(alpha = 0.4f),
        Offset(cx - w * 0.28f, cy + w * 0.12f),
        Offset(cx + w * 0.28f, cy + w * 0.12f),
        w * 0.008f,
        StrokeCap.Round,
    )

    // Stars
    for (i in 0..4) {
        val sx = cx + (i - 2) * w * 0.10f
        val sy = cy + w * 0.20f + sin(i * 1.3f) * w * 0.03f
        val sAlpha = 0.15f + shimmer * 0.15f + i * 0.05f
        drawCircle(rc.gold.copy(alpha = sAlpha.coerceAtMost(1f)), w * 0.008f, Offset(sx, sy))
    }
}

/* ── Page 1: Adhkar — Sun + moon for morning/evening ── */

private fun DrawScope.drawAdhkarIcon(cx: Float, cy: Float, w: Float, shimmer: Float, rc: RafiqPalette) {
    // Sun (morning)
    val sunX = cx - w * 0.10f
    val sunY = cy - w * 0.06f
    val sunR = w * 0.10f
    drawCircle(
        Brush.radialGradient(
            listOf(rc.goldLight, rc.gold),
            Offset(sunX, sunY), sunR
        ),
        sunR, Offset(sunX, sunY),
    )

    // Sun rays
    for (i in 0 until 8) {
        val a = (i * 45 - 90) * PI.toFloat() / 180f
        val r1 = sunR * 1.3f
        val r2 = sunR * 1.7f + shimmer * w * 0.02f
        drawLine(
            rc.gold.copy(alpha = 0.5f),
            Offset(sunX + r1 * cos(a), sunY + r1 * sin(a)),
            Offset(sunX + r2 * cos(a), sunY + r2 * sin(a)),
            w * 0.008f,
            StrokeCap.Round,
        )
    }

    // Moon (evening)
    val moonX = cx + w * 0.14f
    val moonY = cy + w * 0.04f
    val moonR = w * 0.085f
    drawCircle(rc.emerald, moonR, Offset(moonX, moonY))
    drawCircle(rc.emeraldPastel, moonR * 0.75f, Offset(moonX + moonR * 0.30f, moonY - moonR * 0.20f))

    // Stars around moon
    for (i in 0..2) {
        val sa = (i * 50 + 20) * PI.toFloat() / 180f
        val sr = moonR * 2.2f
        val starAlpha = 0.3f + shimmer * 0.3f
        drawCircle(
            rc.emerald.copy(alpha = starAlpha),
            w * 0.010f,
            Offset(moonX + sr * cos(sa), moonY - sr * sin(sa)),
        )
    }

    // Tasbih beads (divider)
    for (i in 0..5) {
        val bx = cx - w * 0.14f + i * w * 0.056f
        val by = cy + w * 0.18f
        drawCircle(
            if (i < 3) rc.gold.copy(alpha = 0.3f) else rc.emerald.copy(alpha = 0.15f),
            w * 0.014f,
            Offset(bx, by),
        )
    }

    // Divider arc
    drawArc(
        rc.inkLight.copy(alpha = 0.20f),
        0f, 180f, false,
        Offset(cx - w * 0.20f, cy + w * 0.13f),
        Size(w * 0.40f, w * 0.12f),
        style = Stroke(w * 0.005f),
    )
}

/* ── Page 2: Quran + Tasbeeh — Book + misbaha ── */

private fun DrawScope.drawQuranIcon(cx: Float, cy: Float, w: Float, shimmer: Float, rc: RafiqPalette) {
    // Book shape 
    val bookW = w * 0.30f
    val bookH = w * 0.38f
    val bookLeft = cx - bookW / 2
    val bookTop = cy - bookH / 2 - w * 0.03f

    // Book shadow
    drawRoundRect(
        rc.emerald.copy(alpha = 0.08f),
        Offset(bookLeft + w * 0.01f, bookTop + w * 0.01f),
        Size(bookW, bookH),
        cornerRadius = CornerRadius(w * 0.025f),
    )

    // Book body
    drawRoundRect(
        rc.emerald,
        Offset(bookLeft, bookTop),
        Size(bookW, bookH),
        cornerRadius = CornerRadius(w * 0.025f),
    )

    // Book spine
    drawLine(
        rc.emeraldMed, 
        Offset(cx, bookTop + w * 0.03f),
        Offset(cx, bookTop + bookH - w * 0.03f),
        w * 0.006f,
    )

    // Gold border
    drawRoundRect(
        rc.gold.copy(alpha = 0.5f + shimmer * 0.3f),
        Offset(bookLeft + w * 0.02f, bookTop + w * 0.02f),
        Size(bookW - w * 0.04f, bookH - w * 0.04f),
        cornerRadius = CornerRadius(w * 0.018f),
        style = Stroke(w * 0.006f),
    )

    // Center ornament
    val ornY = bookTop + bookH / 2
    drawCircle(rc.gold.copy(alpha = 0.6f), w * 0.03f, Offset(cx, ornY))
    drawCircle(rc.emerald, w * 0.018f, Offset(cx, ornY))

    // Top/bottom ornament dots
    drawCircle(rc.gold.copy(alpha = 0.4f), w * 0.010f, Offset(cx, bookTop + w * 0.06f))
    drawCircle(rc.gold.copy(alpha = 0.4f), w * 0.010f, Offset(cx, bookTop + bookH - w * 0.06f))

    // Misbaha (small tasbih below)
    val beadY = cy + w * 0.24f
    val beadCount = 7
    val beadGap = w * 0.042f
    val startX = cx - (beadCount - 1) * beadGap / 2

    // String
    drawLine(
        rc.inkLight.copy(alpha = 0.30f),
        Offset(startX - w * 0.01f, beadY),
        Offset(startX + (beadCount - 1) * beadGap + w * 0.01f, beadY),
        w * 0.004f,
    )

    for (i in 0 until beadCount) {
        val bx = startX + i * beadGap
        val beadColor = if (i % 2 == 0) rc.emerald else rc.gold
        drawCircle(beadColor.copy(alpha = 0.5f), w * 0.016f, Offset(bx, beadY))
    }

    // Tassel
    drawLine(rc.gold.copy(alpha = 0.4f), Offset(cx, beadY), Offset(cx, beadY + w * 0.05f), w * 0.005f, StrokeCap.Round)
    drawCircle(rc.gold.copy(alpha = 0.5f), w * 0.010f, Offset(cx, beadY + w * 0.06f))
}

/* ── Page 3: Start — Rocket / launch ── */

private fun DrawScope.drawStartIcon(cx: Float, cy: Float, w: Float, shimmer: Float, rc: RafiqPalette) {
    val goldShimmer = rc.gold.copy(alpha = 0.5f + shimmer * 0.5f)

    // Large check circle
    val checkR = w * 0.16f
    drawCircle(
        Brush.radialGradient(
            listOf(rc.emerald, rc.emeraldMed),
            Offset(cx, cy - w * 0.02f), checkR
        ),
        checkR,
        Offset(cx, cy - w * 0.02f),
    )

    // Check mark
    val checkPath = Path().apply {
        moveTo(cx - checkR * 0.40f, cy - w * 0.02f)
        lineTo(cx - checkR * 0.05f, cy + checkR * 0.28f - w * 0.02f)
        lineTo(cx + checkR * 0.45f, cy - checkR * 0.30f - w * 0.02f)
    }
    drawPath(
        checkPath, Color.White,
        style = Stroke(w * 0.025f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // Ring
    drawCircle(
        goldShimmer,
        checkR + w * 0.02f,
        Offset(cx, cy - w * 0.02f),
        style = Stroke(w * 0.006f),
    )

    // Sparkles
    val sparkles = listOf(
        Offset(cx - w * 0.24f, cy - w * 0.18f),
        Offset(cx + w * 0.26f, cy - w * 0.14f),
        Offset(cx - w * 0.18f, cy + w * 0.20f),
        Offset(cx + w * 0.20f, cy + w * 0.22f),
        Offset(cx + w * 0.02f, cy - w * 0.28f),
    )

    sparkles.forEachIndexed { i, pos ->
        val sparkAlpha = 0.2f + shimmer * 0.4f + i * 0.06f
        val sz = w * (0.012f + i * 0.003f)

        // Cross sparkle
        drawLine(
            rc.gold.copy(alpha = sparkAlpha.coerceAtMost(1f)),
            Offset(pos.x - sz, pos.y),
            Offset(pos.x + sz, pos.y),
            w * 0.006f,
            StrokeCap.Round,
        )
        drawLine(
            rc.gold.copy(alpha = sparkAlpha.coerceAtMost(1f)),
            Offset(pos.x, pos.y - sz),
            Offset(pos.x, pos.y + sz),
            w * 0.006f,
            StrokeCap.Round,
        )
    }

    // Bottom text area decoration
    drawLine(
        rc.emerald.copy(alpha = 0.15f),
        Offset(cx - w * 0.22f, cy + w * 0.16f),
        Offset(cx + w * 0.22f, cy + w * 0.16f),
        w * 0.005f,
        StrokeCap.Round,
    )

    // Notification bell hint
    val bellX = cx
    val bellY = cy + w * 0.24f

    // Bell body
    drawArc(
        rc.emerald.copy(alpha = 0.3f),
        180f, 180f, true,
        Offset(bellX - w * 0.04f, bellY - w * 0.04f),
        Size(w * 0.08f, w * 0.06f),
    )
    // Bell base
    drawRoundRect(
        rc.emerald.copy(alpha = 0.3f),
        Offset(bellX - w * 0.05f, bellY + w * 0.01f),
        Size(w * 0.10f, w * 0.012f),
        cornerRadius = CornerRadius(w * 0.005f),
    )
    // Clapper
    drawCircle(rc.gold.copy(alpha = 0.4f), w * 0.008f, Offset(bellX, bellY + w * 0.03f))
}
