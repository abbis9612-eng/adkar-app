package app.rafiqaldhikr.ui.screens.settings

import androidx.compose.foundation.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.*

/* ══════════════════════════════════════════════════════════════
   SETTING-SPECIFIC ICON BACKGROUNDS
   (Semantic per-item colors — not part of the shared palette)
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   (Removed hardcoded SettingsBg)
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

@Composable
private fun IconBack(size: Dp = 18.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        drawPath(Path().apply {
            moveTo(w * 0.35f, h * 0.15f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.35f, h * 0.85f)
        }, color, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun IconArrow(size: Dp = 14.dp, color: Color = LocalRafiqColors.current.inkLight) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        drawPath(Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.85f)
        }, color, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// Setting-specific icons drawn in Canvas
@Composable
private fun SettingIcon(type: Int, size: Dp = 20.dp, color: Color = LocalRafiqColors.current.emerald) {
    val rc = LocalRafiqColors.current
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            0 -> { // Palette
                drawCircle(color, w * 0.38f, Offset(cx, cy), style = st)
                drawCircle(color, w * 0.08f, Offset(cx - w * 0.15f, cy - w * 0.12f))
                drawCircle(color, w * 0.07f, Offset(cx + w * 0.12f, cy - w * 0.12f))
                drawCircle(color, w * 0.06f, Offset(cx, cy + w * 0.15f))
                drawCircle(color, w * 0.09f, Offset(cx - w * 0.18f, cy + w * 0.08f))
            }
            1 -> { // T (font)
                drawLine(color, Offset(w * 0.20f, w * 0.22f), Offset(w * 0.80f, w * 0.22f), w * 0.08f, StrokeCap.Round)
                drawLine(color, Offset(cx, w * 0.22f), Offset(cx, w * 0.82f), w * 0.08f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.35f, w * 0.82f), Offset(w * 0.65f, w * 0.82f), w * 0.06f, StrokeCap.Round)
            }
            2 -> { // Bell
                val bell = Path().apply {
                    moveTo(w * 0.23f, w * 0.68f)
                    lineTo(w * 0.30f, w * 0.38f)
                    cubicTo(w * 0.36f, w * 0.22f, w * 0.64f, w * 0.22f, w * 0.70f, w * 0.38f)
                    lineTo(w * 0.77f, w * 0.68f)
                    close()
                }
                drawPath(bell, color.copy(alpha = 0.10f))
                drawPath(bell, color, style = st)
                drawArc(color, 0f, 180f, false,
                    Offset(w * 0.38f, w * 0.68f), Size(w * 0.24f, w * 0.18f), style = st)
            }
            3 -> { // Clock
                drawCircle(color, w * 0.38f, Offset(cx, cy), style = st)
                drawLine(color, Offset(cx, cy), Offset(cx, cy - w * 0.22f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(cx, cy), Offset(cx + w * 0.18f, cy + w * 0.05f), w * 0.07f, StrokeCap.Round)
                drawCircle(color, w * 0.04f, Offset(cx, cy))
            }
            4 -> { // Person (accessibility)
                drawCircle(color, w * 0.13f, Offset(cx, cy - w * 0.18f))
                drawLine(color, Offset(cx, cy - w * 0.05f), Offset(cx, cy + w * 0.22f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(cx - w * 0.22f, cy + w * 0.06f), Offset(cx + w * 0.22f, cy + w * 0.06f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(cx, cy + w * 0.22f), Offset(cx - w * 0.15f, cy + w * 0.40f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(cx, cy + w * 0.22f), Offset(cx + w * 0.15f, cy + w * 0.40f), w * 0.07f, StrokeCap.Round)
            }
            5 -> { // Globe (language)
                drawCircle(color, w * 0.36f, Offset(cx, cy), style = st)
                drawArc(color, 0f, 360f, false,
                    Offset(cx - w * 0.20f, cy - w * 0.36f), Size(w * 0.40f, w * 0.72f), style = Stroke(w * 0.04f))
                drawLine(color, Offset(w * 0.14f, cy), Offset(w * 0.86f, cy), w * 0.04f)
            }
            6 -> { // Star (premium)
                val star = Path().apply {
                    for (i in 0 until 10) {
                        val a = (i * 36 - 90) * PI.toFloat() / 180f
                        val r = if (i % 2 == 0) w * 0.40f else w * 0.20f
                        if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                        else lineTo(cx + r * cos(a), cy + r * sin(a))
                    }; close()
                }
                drawPath(star, rc.goldLight.copy(alpha = 0.18f))
                drawPath(star, rc.goldLight, style = Stroke(w * 0.06f, join = StrokeJoin.Round))
            }
            7 -> { // Widget
                drawRoundRect(color, Offset(w * 0.10f, w * 0.10f), Size(w * 0.35f, w * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
                drawRoundRect(color, Offset(w * 0.55f, w * 0.10f), Size(w * 0.35f, w * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
                drawRoundRect(color, Offset(w * 0.10f, w * 0.55f), Size(w * 0.35f, w * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
                drawRoundRect(color, Offset(w * 0.55f, w * 0.55f), Size(w * 0.35f, w * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
            }
            8 -> { // Upload/Export
                drawLine(color, Offset(cx, w * 0.18f), Offset(cx, w * 0.60f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.32f, w * 0.38f), Offset(cx, w * 0.18f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.68f, w * 0.38f), Offset(cx, w * 0.18f), w * 0.07f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.20f, w * 0.80f), Offset(w * 0.80f, w * 0.80f), w * 0.07f, StrokeCap.Round)
            }
            9 -> { // Info
                drawCircle(color, w * 0.38f, Offset(cx, cy), style = st)
                drawCircle(color, w * 0.05f, Offset(cx, cy - w * 0.14f))
                drawLine(color, Offset(cx, cy - w * 0.02f), Offset(cx, cy + w * 0.22f), w * 0.07f, StrokeCap.Round)
            }
            10 -> { // Help (question mark)
                drawCircle(color, w * 0.38f, Offset(cx, cy), style = st)
                drawArc(color, 200f, 170f, false,
                    Offset(cx - w * 0.14f, cy - w * 0.22f), Size(w * 0.28f, w * 0.24f), style = st)
                drawLine(color, Offset(cx, cy + w * 0.02f), Offset(cx, cy + w * 0.10f), w * 0.06f, StrokeCap.Round)
                drawCircle(color, w * 0.04f, Offset(cx, cy + w * 0.22f))
            }
            11 -> { // New releases / sparkle
                drawCircle(color, w * 0.05f, Offset(cx, cy - w * 0.30f))
                drawLine(color, Offset(cx, cy - w * 0.38f), Offset(cx, cy - w * 0.22f), w * 0.05f, StrokeCap.Round)
                drawLine(color, Offset(cx - w * 0.06f, cy - w * 0.30f), Offset(cx + w * 0.06f, cy - w * 0.30f), w * 0.05f, StrokeCap.Round)
                val star = Path().apply {
                    for (i in 0 until 8) {
                        val a = (i * 45 - 90) * PI.toFloat() / 180f
                        val r = if (i % 2 == 0) w * 0.30f else w * 0.16f
                        if (i == 0) moveTo(cx + r * cos(a), cy + w * 0.08f + r * sin(a))
                        else lineTo(cx + r * cos(a), cy + w * 0.08f + r * sin(a))
                    }; close()
                }
                drawPath(star, color.copy(alpha = 0.12f))
                drawPath(star, color, style = Stroke(w * 0.05f, join = StrokeJoin.Round))
            }
            12 -> { // Privacy / shield
                val shield = Path().apply {
                    moveTo(cx, w * 0.10f)
                    lineTo(w * 0.82f, w * 0.25f)
                    lineTo(w * 0.78f, w * 0.60f)
                    cubicTo(w * 0.72f, w * 0.80f, cx, w * 0.92f, cx, w * 0.92f)
                    cubicTo(cx, w * 0.92f, w * 0.28f, w * 0.80f, w * 0.22f, w * 0.60f)
                    lineTo(w * 0.18f, w * 0.25f)
                    close()
                }
                drawPath(shield, color.copy(alpha = 0.10f))
                drawPath(shield, color, style = st)
            }
            13 -> { // Terms / document
                drawRoundRect(color, Offset(w * 0.20f, w * 0.10f), Size(w * 0.60f, w * 0.80f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
                drawLine(color.copy(alpha = 0.5f), Offset(w * 0.32f, w * 0.32f), Offset(w * 0.68f, w * 0.32f), w * 0.05f, StrokeCap.Round)
                drawLine(color.copy(alpha = 0.5f), Offset(w * 0.32f, w * 0.48f), Offset(w * 0.60f, w * 0.48f), w * 0.05f, StrokeCap.Round)
                drawLine(color.copy(alpha = 0.5f), Offset(w * 0.32f, w * 0.64f), Offset(w * 0.55f, w * 0.64f), w * 0.05f, StrokeCap.Round)
            }
            14 -> { // Email
                drawRoundRect(color, Offset(w * 0.10f, w * 0.22f), Size(w * 0.80f, w * 0.56f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = st)
                drawLine(color, Offset(w * 0.10f, w * 0.22f), Offset(cx, cy), w * 0.06f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.90f, w * 0.22f), Offset(cx, cy), w * 0.06f, StrokeCap.Round)
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   SETTING ITEM DATA
══════════════════════════════════════════════════════════════ */

private data class SettingItem(
    val iconType: Int,
    val label: String,
    val baseColor: (app.rafiqaldhikr.ui.theme.RafiqPalette) -> Color,
    val badge: String? = null,
    val route: String,
)

private val GROUP_1 = listOf(
    SettingItem(0,  "إعدادات المظهر",    { it.gold }, route = "theme_settings"),
    SettingItem(1,  "إعدادات الخط",      { it.emerald }, route = "font_settings"),
    SettingItem(2,  "إعدادات الإشعارات", { it.inkMed }, route = "notification_settings"),
    SettingItem(3,  "طريقة حساب الصلاة", { it.brownAccent }, route = "prayer_method"),
    SettingItem(4,  "إمكانية الوصول",    { it.emerald }, route = "accessibility_settings"),
    SettingItem(5,  "اللغة",             { it.gold }, route = "language"),
)

private val GROUP_2 = listOf(
    SettingItem(6,  "المميز",            { it.gold }, badge = "PRO", route = "premium"),
    SettingItem(7,  "إعدادات الودجت",    { it.emerald }, route = "widget_settings"),
    SettingItem(8,  "تصدير البيانات",    { it.brownAccent }, route = "export_data"),
)

private val GROUP_3 = listOf(
    SettingItem(9,  "حول التطبيق",       { it.inkMed }, route = "about"),
    SettingItem(10, "المساعدة",          { it.brownAccent }, route = "help"),
    SettingItem(11, "ما الجديد",         { it.emerald }, route = "whats_new"),
    SettingItem(12, "سياسة الخصوصية",    { it.gold }, route = "privacy_policy"),
    SettingItem(13, "شروط الاستخدام",    { it.inkMed }, route = "terms"),
    SettingItem(14, "تواصل معنا",        { it.gold }, route = "contact"),
)

/* ══════════════════════════════════════════════════════════════
   SETTINGS GROUP — Grouped white card with items
══════════════════════════════════════════════════════════════ */

@Composable
private fun SettingsGroup(items: List<SettingItem>, navController: NavHostController) {
    val rc = LocalRafiqColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
    ) {
        items.forEachIndexed { idx, item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(item.route) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon circle
                val baseCol = item.baseColor(rc)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(baseCol.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    SettingIcon(type = item.iconType, size = 18.dp, color = baseCol)
                }

                Spacer(Modifier.width(14.dp))

                // Label
                Text(
                    item.label,
                    fontSize = 16.sp,
                    color = rc.ink, modifier = Modifier.weight(1f),
                )

                // Pro badge
                if (item.badge != null) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(rc.goldLight)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(item.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                // Arrow
                IconArrow(14.dp, rc.inkLight)
            }

            // Separator
            if (idx < items.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(rc.gold.copy(alpha = 0.06f))
                )
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN SETTINGS SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun SettingsScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current
    val scrollState = rememberScrollState()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // ═══ TOP BAR ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Back button
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) { IconBack() }

                Text("الإعدادات", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
            }

            Spacer(Modifier.height(8.dp))

            // ═══ GROUP 1 — المظهر والعرض ═══
            SettingsGroup(GROUP_1, navController)

            Spacer(Modifier.height(14.dp))

            // ═══ GROUP 2 — المميز والبيانات ═══
            SettingsGroup(GROUP_2, navController)

            Spacer(Modifier.height(14.dp))

            // ═══ GROUP 3 — المعلومات ═══
            SettingsGroup(GROUP_3, navController)

            Spacer(Modifier.height(28.dp))
        }
    }
}
