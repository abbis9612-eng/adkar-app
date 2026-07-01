package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Prayer-time-aware Live Sky Background.
 * Changes gradient colors based on current prayer time:
 * - Fajr (4-6):   Deep indigo → soft pink → warm gold horizon
 * - Sunrise (6-7): Pink → orange → gold
 * - Dhuhr (12-15): Light sky blue → warm white
 * - Asr (15-17):   Golden sky → warm amber
 * - Maghrib (17-19): Deep orange → purple → indigo
 * - Isha (19-4):   Deep navy → dark purple → midnight
 *
 * ✅ Optimized: removed paper grain (200 circles per frame).
 */

enum class PrayerTimePeriod {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}

fun getCurrentPrayerPeriod(): PrayerTimePeriod {
    val hour = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 4..5   -> PrayerTimePeriod.FAJR
        in 6..6   -> PrayerTimePeriod.SUNRISE
        in 7..14  -> PrayerTimePeriod.DHUHR
        in 15..16 -> PrayerTimePeriod.ASR
        in 17..18 -> PrayerTimePeriod.MAGHRIB
        else      -> PrayerTimePeriod.ISHA
    }
}

data class SkyPalette(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val accent: Color,
    val ambientGlow: Color
)

fun getSkyPalette(period: PrayerTimePeriod, rc: RafiqPalette): SkyPalette = when (period) {
    PrayerTimePeriod.FAJR -> SkyPalette(
        top = Color(0xFF1A1A3E),
        middle = Color(0xFF4A3060),
        bottom = Color(0xFFD4856A),
        accent = Color(0xFFF0C96E),
        ambientGlow = Color(0xFFF4A735)
    )
    PrayerTimePeriod.SUNRISE -> SkyPalette(
        top = Color(0xFF3D5A80),
        middle = Color(0xFFE88C6A),
        bottom = Color(0xFFF0C96E),
        accent = Color(0xFFFFD700),
        ambientGlow = Color(0xFFFF9E50)
    )
    PrayerTimePeriod.DHUHR -> SkyPalette(
        top = Color(0xFF87CEEB).copy(alpha = 0.3f),
        middle = Color(0xFFFDF8F0),
        bottom = Color(0xFFF8F2E6),
        accent = rc.gold,
        ambientGlow = Color(0xFF87CEEB)
    )
    PrayerTimePeriod.ASR -> SkyPalette(
        top = Color(0xFFF8F2E6),
        middle = Color(0xFFF4EDD8),
        bottom = Color(0xFFE8C87A),
        accent = Color(0xFFD4A843),
        ambientGlow = Color(0xFFF0C96E)
    )
    PrayerTimePeriod.MAGHRIB -> SkyPalette(
        top = Color(0xFF2C1654),
        middle = Color(0xFFC14E3A),
        bottom = Color(0xFFF0883A),
        accent = Color(0xFFFFB347),
        ambientGlow = Color(0xFFFF6B35)
    )
    PrayerTimePeriod.ISHA -> SkyPalette(
        top = Color(0xFF0A0F1E),
        middle = Color(0xFF121830),
        bottom = Color(0xFF1A2040),
        accent = Color(0xFF4A5080),
        ambientGlow = Color(0xFF2D3060)
    )
}

@Composable
fun PrayerSkyBackground(
    modifier: Modifier = Modifier,
    period: PrayerTimePeriod = getCurrentPrayerPeriod(),
    content: @Composable () -> Unit
) {
    val rc = LocalRafiqColors.current
    val palette = getSkyPalette(period, rc)

    val transition = rememberInfiniteTransition(label = "skyAmbient")

    // Ambient light position
    val ambientX by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientX"
    )
    val ambientY by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientY"
    )
    val ambientAlpha by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientAlpha"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Main sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.top, palette.middle, palette.bottom)
                )
            )

            // Ambient glow blob (top)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.ambientGlow.copy(alpha = ambientAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width * ambientX, size.height * ambientY),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * ambientX, size.height * ambientY)
            )

            // Secondary glow blob (bottom)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accent.copy(alpha = ambientAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * (1f - ambientX), size.height * 0.8f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * (1f - ambientX), size.height * 0.8f)
            )

            // ✅ Paper grain removed — was 200 drawCircle calls per frame
            //    with alpha 0.015f (invisible). Saved ~200 draw calls/frame.
        }

        content()
    }
}

/**
 * Parchment-style background for light theme — warm paper texture.
 * ✅ Optimized: static glow (no infinite animation).
 *    The alpha difference (0.04 → 0.10) was imperceptible;
 *    a fixed 0.07 looks identical and saves an entire animation.
 */
@Composable
fun ParchmentBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val rc = LocalRafiqColors.current
    val glowAlpha = 0.07f  // ✅ Static — was animated 0.04→0.10, difference invisible

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Warm parchment gradient
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDF8F0),
                        Color(0xFFF8F2E6),
                        Color(0xFFF4EDD8)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.8f, size.height)
                )
            )

            // Green ambient glow (top-right) — static
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        rc.emerald.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )

            // Gold ambient glow (bottom-left) — static
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        rc.gold.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.15f, size.height * 0.85f)
            )
        }

        content()
    }
}
