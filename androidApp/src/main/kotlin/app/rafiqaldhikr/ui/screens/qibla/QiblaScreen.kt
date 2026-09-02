package app.rafiqaldhikr.ui.screens.qibla

import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.NeedsLocation
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin
import app.rafiqaldhikr.ui.components.IcoAlert
import app.rafiqaldhikr.ui.components.IcoCompass
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import kotlin.math.roundToInt
import kotlin.math.PI
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RIcon
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.animation.animateColorAsState

@Composable
fun QiblaScreen(
    navController: NavHostController,
    viewModel: QiblaViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    // Smooth rotation animation
    val animatedRotation by animateFloatAsState(
        targetValue   = state.rotationToQibla,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "qiblaRotation"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // u2550u2550u2550 HEADER u2550u2550u2550
            RafiqTopBar(
                title  = stringResource(R.string.qibla_title),
                onBack = {navController.popBackStack()},
            )

            Column(
                modifier            = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    !state.isCompassAvailable -> NoCompassContent(rc)
                    // NoLocationContent كانت نصّاً ميّتاً: isLocationKnown لم تكن
                    // تصير false أبداً لأن الاحتياطيّ كان يملأ الإحداثيات دوماً.
                    !state.isLocationKnown    -> NeedsLocation(
                        message = "اتجاه القبلة يُحسب من موقعك، وسهمٌ يشير من مدينةٍ أخرى يشير إلى غير الكعبة."
                    )
                    state.error != null       -> ErrorContent(state.error!!, rc)
                    else                      -> QiblaCompassContent(
                        state    = state,
                        rotation = animatedRotation,
                        ar       = LocalArabicNumerals.current,
                        rc       = rc,
                    )
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   ممرُّ النور

   كان سهماً يشير — وسهمٌ يشير يترك صاحبَه يحزر متى استقام. فصار
   الأمرُ مهمّةً بصرية: ممرٌّ يخرج من المركز إلى أعلى الجهاز، والكعبةُ
   علامةٌ على الحافّة تدور مع الاتّجاه. تُدير حتى تدخل الكعبةُ في
   الممرّ، فتخضرُّ الدائرةُ ويقول النصُّ «استقبِلْ وصلِّ».

   ولا يُعلَن ذلك حتى تصدق بوّابتا الثقة — راجع [QiblaViewModel.UiState].
══════════════════════════════════════════════════════════════ */

private const val ALIGNED_DEG = 3f
private const val NEAR_DEG = 12f

/** فرقٌ زاويٌّ في المدى ‎[−180، 180]. */
private fun signedDelta(d: Float): Float = ((d + 540f) % 360f) - 180f

@Composable
private fun QiblaCompassContent(
    state: QiblaViewModel.UiState,
    rotation: Float,
    ar: Boolean,
    rc: RafiqPalette,
) {
    val delta = signedDelta(rotation)
    val abs = kotlin.math.abs(delta)
    val aligned = abs <= ALIGNED_DEG && state.trustworthy
    val near = abs <= NEAR_DEG

    val accent by animateColorAsState(
        if (aligned) rc.emerald else rc.goldLight,
        tween(320), label = "qiblaAccent",
    )

    /*  البوصلةُ كلُّها كانت `Canvas` بلا دلالاتٍ واحدة.
     *
     *  أي أنّ مستعمل TalkBack لا يسمع شيئاً على شاشة القبلة إطلاقاً: لا
     *  الاتّجاه، ولا كم بقي، ولا أنّه استقام. وهي شاشةٌ الغرضُ منها كلُّه
     *  توجيهٌ — فمن لا يبصر أحوجُ الناس إليها.
     *
     *  والنصُّ حيٌّ يتغيّر مع الدوران، فيقرأ قارئُ الشاشة تقدّمَه.  */
    val spoken = when {
        aligned  -> stringResource(R.string.qibla_a11y_aligned)
        else     -> stringResource(
            R.string.qibla_a11y_turn,
            kotlin.math.abs(delta).toInt().localized(ar),
            stringResource(
                if (delta > 0) R.string.qibla_a11y_left else R.string.qibla_a11y_right
            ),
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = spoken
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        /* الحالة */
        Row(
            Modifier
                .clip(CircleShape)
                .background(if (aligned) rc.emeraldPastel else if (near) rc.tintGold else rc.card)
                .border(
                    1.dp,
                    if (aligned) rc.emerald.copy(alpha = 0.30f) else rc.divider,
                    CircleShape,
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(9.dp))
            Text(
                when {
                    !state.trustworthy -> "لا نُعلن الاتّجاه حتى تستقرّ البوصلة"
                    aligned -> "ثبتَ الاتّجاه — استقبِلْ وصلِّ"
                    near    -> "اقتربت — حرّكه ببطء"
                    else    -> "أدِرِ الهاتفَ حتى تدخل الكعبةُ في الممرّ"
                },
                style = RafiqType.bodyS,
                color = if (aligned) rc.emerald else rc.inkMed,
                maxLines = 1,
            )
        }

        Spacer(Modifier.height(12.dp))
        QiblaDial(delta = delta, heading = state.deviceHeading, aligned = aligned, accent = accent, rc = rc)

        Spacer(Modifier.height(6.dp))
        Text("فرقُ الاتّجاه الآن", style = RafiqType.bodyS, color = rc.gold)
        if (aligned) {
            Text("استقبِلْ وصلِّ", style = RafiqType.hero, color = rc.emerald)
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${kotlin.math.abs(delta).roundToInt().localized(ar)}°",
                    style = RafiqType.hero, color = rc.emerald)
                Spacer(Modifier.width(7.dp))
                Text(
                    if (delta > 0) "يميناً" else "يساراً",
                    style = RafiqType.titleM, color = rc.emerald,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        TrustGates(state, rc)

        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Fact("${state.qiblaBearing.roundToInt().localized(ar)}°", "زاويةُ القبلة", Modifier.weight(1f), rc)
            Fact(state.distanceKm.localized(ar), "كم إلى مكّة", Modifier.weight(1f), rc)
        }
    }
}

@Composable
private fun QiblaDial(
    delta: Float,
    heading: Float,
    aligned: Boolean,
    accent: Color,
    rc: RafiqPalette,
) {
    Canvas(Modifier.size(288.dp)) {
        val w = size.width
        val c = Offset(w / 2f, w / 2f)
        val R = w / 2f - 10.dp.toPx()

        fun polar(angleDeg: Float, r: Float) = Offset(
            c.x + r * sin(angleDeg * PI / 180.0).toFloat(),
            c.y - r * cos(angleDeg * PI / 180.0).toFloat(),
        )

        drawCircle(if (aligned) rc.emeraldPastel else rc.card, R, c)
        drawCircle(
            if (aligned) rc.emerald else rc.cardBorder, R, c,
            style = Stroke(if (aligned) 3.dp.toPx() else 1.5.dp.toPx()),
        )

        // علاماتُ الدرجات كلَّ 7.5° — والأطولُ عند الأرباع
        var wd = 0f
        while (wd < 360f) {
            val a = signedDelta(wd - heading)
            val major = wd % 45f == 0f
            drawLine(
                if (major) rc.inkMed else rc.divider,
                polar(a, R - (if (major) 18 else 13).dp.toPx()),
                polar(a, R - 5.dp.toPx()),
                (if (major) 2 else 1).dp.toPx(),
                StrokeCap.Round,
            )
            wd += 7.5f
        }

        /*  الممرّ: يخرج من المركز إلى أعلى الجهاز ويتّسع صعوداً، ويتلاشى
            نزولاً. وهو الهدفُ الذي تُدخل الكعبةَ فيه. */
        val top = c.y - R + 18.dp.toPx()
        val beam = Path().apply {
            moveTo(c.x - 5.dp.toPx(), c.y)
            lineTo(c.x - 14.dp.toPx(), top + 18.dp.toPx())
            quadraticBezierTo(c.x, top, c.x + 14.dp.toPx(), top + 18.dp.toPx())
            lineTo(c.x + 5.dp.toPx(), c.y)
            close()
        }
        drawPath(
            beam,
            Brush.verticalGradient(
                0f to accent.copy(alpha = 0.55f),
                0.55f to accent.copy(alpha = 0.18f),
                1f to Color.Transparent,
                startY = top, endY = c.y,
            ),
        )
        // رأسُ الممرّ عند حافّة الجهاز
        drawPath(
            Path().apply {
                moveTo(c.x - 13.dp.toPx(), c.y - R - 2.dp.toPx())
                lineTo(c.x + 13.dp.toPx(), c.y - R - 2.dp.toPx())
                lineTo(c.x + 7.dp.toPx(), c.y - R + 10.dp.toPx())
                lineTo(c.x - 7.dp.toPx(), c.y - R + 10.dp.toPx())
                close()
            },
            accent,
        )

        // الكعبةُ على الحافّة، تدور مع الاتّجاه
        val k = polar(delta, R - 24.dp.toPx())
        drawCircle(accent.copy(alpha = 0.13f), 25.dp.toPx(), k)
        val s = 16.dp.toPx()
        drawRoundRect(
            if (aligned) rc.emerald else rc.ink,
            topLeft = Offset(k.x - s, k.y - s),
            size = Size(s * 2, s * 2),
            cornerRadius = CornerRadius(6.dp.toPx()),
        )
        drawRect(rc.goldLight, Offset(k.x - s, k.y - s + s * 0.5f), Size(s * 2, s * 0.42f))
        drawRoundRect(
            Color(0xFFE0C37A),
            topLeft = Offset(k.x - s * 0.32f, k.y + s * 0.30f),
            size = Size(s * 0.64f, s * 0.70f),
            cornerRadius = CornerRadius(1.5.dp.toPx()),
        )

        drawCircle(if (aligned) rc.emerald else rc.inkDark, 14.dp.toPx(), c)
        drawCircle(rc.goldLight, 4.dp.toPx(), c)
    }
}

@Composable
private fun TrustGates(state: QiblaViewModel.UiState, rc: RafiqPalette) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.divider, RafiqShape.card)
            .padding(13.dp),
    ) {
        Row {
            RafiqIcon(RIcon.Check, 19.dp, rc.emerald)
            Spacer(Modifier.width(9.dp))
            Column {
                Text("لا نُعلن الاتّجاه قبل أن تصدق الاثنتان",
                    style = RafiqType.label, color = rc.ink)
                Text("وبوصلةُ الهاتف تخطئ قربَ المعدن والكهرباء.",
                    style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Gate("البوصلةُ مضبوطة", state.compassTrusted, Modifier.weight(1f), rc)
            Gate("القراءةُ ثابتة", state.readingSteady, Modifier.weight(1f), rc)
        }
    }
}

@Composable
private fun Gate(label: String, ok: Boolean, modifier: Modifier, rc: RafiqPalette) {
    Row(
        modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (ok) rc.emeraldPastel else rc.chipBg)
            .border(1.dp, if (ok) rc.emerald.copy(alpha = 0.16f) else rc.divider, RoundedCornerShape(11.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (ok) {
            RafiqIcon(RIcon.Check, 14.dp, rc.emerald)
            Spacer(Modifier.width(5.dp))
        }
        Text(
            if (ok) label else "$label…",
            style = RafiqType.caption,
            color = if (ok) rc.emerald else rc.inkMed,
            maxLines = 1,
        )
    }
}

@Composable
private fun Fact(value: String, label: String, modifier: Modifier, rc: RafiqPalette) {
    Column(
        modifier
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.divider, RafiqShape.card)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Text(value, style = RafiqType.titleL, color = rc.emerald)
        Text(label, style = RafiqType.bodyS, color = rc.inkMed)
    }
}

@Composable
private fun NoCompassContent(rc: RafiqPalette) {
    IcoCompass(80.dp, rc.error, off = true)
    Spacer(Modifier.height(16.dp))
    Text("البوصلة غير متوفرة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
    Spacer(Modifier.height(8.dp))
    Text("جهازك لا يدعم مستشعر البوصلة", textAlign = TextAlign.Center, color = rc.inkMed, style = RafiqType.body)
}

@Composable
private fun ErrorContent(message: String, rc: RafiqPalette) {
    IcoAlert(64.dp, rc.error)
    Spacer(Modifier.height(16.dp))
    Text(message, textAlign = TextAlign.Center, color = rc.ink, style = RafiqType.body)
}
