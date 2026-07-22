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
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon

/* ══════════════════════════════════════════════════════════════
   SETTING-SPECIFIC ICON BACKGROUNDS
   (Semantic per-item colors — not part of the shared palette)
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   (Removed hardcoded SettingsBg)
══════════════════════════════════════════════════════════════ */


/* ══════════════════════════════════════════════════════════════
   SETTING ITEM DATA
══════════════════════════════════════════════════════════════ */

private data class SettingItem(
    val icon: RIcon,
    val label: String,
    val baseColor: (app.rafiqaldhikr.ui.theme.RafiqPalette) -> Color,
    val badge: String? = null,
    val route: String,
)

private val GROUP_1 = listOf(
    SettingItem(RIcon.Palette,  "إعدادات المظهر",    { it.gold }, route = "theme_settings"),
    SettingItem(RIcon.Font,     "إعدادات الخط",      { it.emerald }, route = "font_settings"),
    SettingItem(RIcon.Bell,     "إعدادات الإشعارات", { it.inkMed }, route = "notification_settings"),
    SettingItem(RIcon.Clock,    "طريقة حساب الصلاة", { it.brownAccent }, route = "prayer_method"),
    SettingItem(RIcon.User,     "إمكانية الوصول",    { it.emerald }, route = "accessibility_settings"),
    SettingItem(RIcon.Globe,    "اللغة",             { it.gold }, route = "language"),
)

private val GROUP_2 = listOf(
    // "المميز" (route = "premium") مخفي مؤقتاً حتى يكتمل ربط RevenueCat —
    // الشاشة الحالية أزرارها غير فعّالة (شراء/استعادة) والمتاجر ترفض ذلك.
    SettingItem(RIcon.Widget,   "إعدادات الودجت",    { it.emerald }, route = "widget_settings"),
    SettingItem(RIcon.Upload,   "تصدير البيانات",    { it.brownAccent }, route = "export_data"),
)

private val GROUP_3 = listOf(
    SettingItem(RIcon.Info,      "حول التطبيق",       { it.inkMed }, route = "about"),
    SettingItem(RIcon.Help,      "المساعدة",          { it.brownAccent }, route = "help"),
    SettingItem(RIcon.Sparkles,  "ما الجديد",         { it.emerald }, route = "whats_new"),
    SettingItem(RIcon.Shield,    "سياسة الخصوصية",    { it.gold }, route = "privacy_policy"),
    SettingItem(RIcon.Document,  "شروط الاستخدام",    { it.inkMed }, route = "terms"),
    SettingItem(RIcon.Mail,      "تواصل معنا",        { it.gold }, route = "contact"),
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
                    RafiqIcon(item.icon, 18.dp, baseCol)
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

                // Arrow (chevron يسار — اتجاه الدخول في RTL)
                RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
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
                RafiqBackButton(onClick = { navController.popBackStack() })

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
