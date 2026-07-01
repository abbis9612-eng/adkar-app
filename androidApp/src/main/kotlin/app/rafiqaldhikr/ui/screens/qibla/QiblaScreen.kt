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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width; val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f); lineTo(w * 0.70f, h * 0.50f); lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }

                Text(
                    text = stringResource(R.string.qibla_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            Column(
                modifier            = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    !state.isCompassAvailable -> NoCompassContent(rc)
                    !state.isLocationKnown    -> NoLocationContent(rc)
                    state.error != null       -> ErrorContent(state.error!!, rc)
                    else                      -> QiblaCompassContent(
                        bearing          = state.qiblaBearing,
                        heading          = state.deviceHeading,
                        rotationToQibla  = animatedRotation,
                        rc               = rc
                    )
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassContent(
    bearing:         Float,
    heading:         Float,
    rotationToQibla: Float,
    rc:              RafiqPalette
) {
    val primaryColor   = rc.emerald
    val onSurfaceColor = rc.ink

    // Kaaba icon rotated towards Qibla
    Box(
        modifier         = Modifier.size(280.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(rc.card)
            .border(2.dp, rc.gold.copy(alpha = 0.15f), CircleShape)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Compass rose background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f - 8.dp.toPx()

            // Outer circle
            drawCircle(
                color  = primaryColor.copy(alpha = 0.05f),
                radius = r,
                center = Offset(cx, cy)
            )
            drawCircle(
                color  = primaryColor.copy(alpha = 0.2f),
                radius = r,
                style  = Stroke(width = 2.dp.toPx()),
                center = Offset(cx, cy)
            )

            // Cardinal direction marks (N, S, E, W)
            for (i in 0 until 360 step 45) {
                val angleRad = Math.toRadians(i.toDouble() - heading)
                val innerR   = if (i % 90 == 0) r - 20.dp.toPx() else r - 12.dp.toPx()
                val startX   = (cx + innerR * sin(angleRad)).toFloat()
                val startY   = (cy - innerR * cos(angleRad)).toFloat()
                val endX     = (cx + r * sin(angleRad)).toFloat()
                val endY     = (cy - r * cos(angleRad)).toFloat()
                drawLine(
                    color       = onSurfaceColor.copy(alpha = 0.2f),
                    start       = Offset(startX, startY),
                    end         = Offset(endX, endY),
                    strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.dp.toPx()
                )
            }

            // Qibla arrow — pointing toward Qibla bearing
            val qiblaAngleRad = Math.toRadians((bearing - heading + 360).rem(360.0))
            val arrowLen      = r - 24.dp.toPx()
            val arrowEndX     = (cx + arrowLen * sin(qiblaAngleRad)).toFloat()
            val arrowEndY     = (cy - arrowLen * cos(qiblaAngleRad)).toFloat()

            // Arrow body
            drawLine(
                color       = primaryColor,
                start       = Offset(cx, cy),
                end         = Offset(arrowEndX, arrowEndY),
                strokeWidth = 4.dp.toPx()
            )

            // Arrow head
            val headLen   = 16.dp.toPx()
            val headAngle = 35.0
            val headL = Math.toRadians(Math.toDegrees(qiblaAngleRad) + 180 + headAngle)
            val headR = Math.toRadians(Math.toDegrees(qiblaAngleRad) + 180 - headAngle)
            val arrowPath = Path().apply {
                moveTo(arrowEndX, arrowEndY)
                lineTo(
                    (arrowEndX + headLen * sin(headL)).toFloat(),
                    (arrowEndY - headLen * cos(headL)).toFloat()
                )
                lineTo(
                    (arrowEndX + headLen * sin(headR)).toFloat(),
                    (arrowEndY - headLen * cos(headR)).toFloat()
                )
                close()
            }
            drawPath(arrowPath, color = primaryColor, style = Fill)
        }

        // Center Kaaba icon
        Surface(
            shape  = CircleShape,
            color  = rc.emerald.copy(alpha = 0.1f),
            modifier = Modifier.size(60.dp)
        ) {
            Icon(
                Icons.Default.Mosque,
                contentDescription = "اتجاه القبلة",
                modifier = Modifier.padding(14.dp),
                tint     = rc.emerald
            )
        }
    }

    Spacer(Modifier.height(48.dp))

    Text(
        text  = stringResource(R.string.qibla_direction),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = rc.ink
    )
    Spacer(Modifier.height(12.dp))

    Text(
        text  = "الزاوية: ${bearing.toInt()}°",
        fontSize = 18.sp,
        color = rc.inkMed
    )
    Spacer(Modifier.height(12.dp))

    Text(
        text      = stringResource(R.string.calibrate_compass),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = rc.inkLight
    )
}

@Composable
private fun NoCompassContent(rc: RafiqPalette) {
    Icon(Icons.Default.ExploreOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFFE53935))
    Spacer(Modifier.height(16.dp))
    Text("البوصلة غير متوفرة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
    Spacer(Modifier.height(8.dp))
    Text("جهازك لا يدعم مستشعر البوصلة", fontSize = 16.sp, textAlign = TextAlign.Center, color = rc.inkMed)
}

@Composable
private fun NoLocationContent(rc: RafiqPalette) {
    Icon(Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = rc.gold)
    Spacer(Modifier.height(16.dp))
    Text("الموقع غير محدد", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
    Spacer(Modifier.height(8.dp))
    Text("يرجى السماح بالوصول للموقع من إعدادات الصلاة", fontSize = 16.sp, textAlign = TextAlign.Center, color = rc.inkMed)
}

@Composable
private fun ErrorContent(message: String, rc: RafiqPalette) {
    Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE53935))
    Spacer(Modifier.height(16.dp))
    Text(message, fontSize = 16.sp, textAlign = TextAlign.Center, color = rc.ink)
}
