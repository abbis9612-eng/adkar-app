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
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.*
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

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
    // معرّف مورد لا نصّ: القوائم هنا تُبنى في مستوى الملف، خارج أي
    // @Composable، فلا يمكن استدعاء stringResource عندها. المعرّف يُحلّ
    // وقت العرض حيث السياق متاح.
    @StringRes val label: Int,
    val baseColor: (app.rafiqaldhikr.ui.theme.RafiqPalette) -> Color,
    val badge: String? = null,
    val route: String,
)

/*  خمسُ نبراتٍ لها معنى، لا لونٌ واحد ولا قوسُ قزح.
 *
 *  كانت ثلاثاً: gold وemerald وinkMed. ومنذ صارت الهويّة عنبريةً صار
 *  gold وemerald بنّيَّين متقاربين، وinkMed رمادياً — فبدت القائمة
 *  لوناً واحداً باهتاً ومربّعاً رمادياً بينها.
 *
 *  والخمسُ كلُّها توكنز موجودة أصلاً في اللوحة ولها دلالة:
 *    emerald   → الهويّة (المظهر · الألوان · المساعدة)
 *    lightNight→ القراءة والمعرفة (الخطّ · اللغة · حول · تواصل)
 *    lightDusk → التنبيه والوقت (التنبيهات · الشروط)
 *    success   → الأمان والإتمام (الوصول · التصدير · الخصوصية)
 *    gold      → التمييز (طريقة الحساب · ما الجديد)
 */
private val GROUP_1 = listOf(
    SettingItem(RIcon.Palette,  R.string.settings_theme,          { it.emerald },    route = "theme_settings"),
    SettingItem(RIcon.Font,     R.string.settings_font,           { it.lightNight }, route = "font_settings"),
    SettingItem(RIcon.Bell,     R.string.settings_notifications,  { it.lightDusk },  route = "notification_settings"),
    SettingItem(RIcon.Clock,    R.string.settings_prayer_method,  { it.gold },       route = "prayer_method"),
    SettingItem(RIcon.User,     R.string.settings_accessibility,  { it.success },    route = "accessibility_settings"),
    SettingItem(RIcon.Globe,    R.string.settings_language,       { it.lightNight }, route = "language"),
    SettingItem(RIcon.Palette,  R.string.settings_colors,         { it.emerald },    route = "colors"),
)

private val GROUP_2 = listOf(
    // شاشة "المميز" حُذفت في ط٠ — كانت أزرارها placeholder والمتاجر ترفض ذلك.
    //  و"إعدادات الودجت" حُذفت: مفاتيحُها الثلاثة `remember` لا تُحفظ في
    //  أيّ مكان، واثنان منها لودجتَين لم تُكتبا قطّ (لا صنفَ ولا مستقبِلَ
    //  ولا XML — الودجتُ الوحيد PrayerWidget)، والثالثُ «تفعيل» لشيءٍ لا
    //  يملك التطبيقُ تفعيلَه: الودجتَ يُضيفه المستخدمُ من الشاشة الرئيسية.
    SettingItem(RIcon.Upload,   R.string.settings_export,         { it.success },    route = "export_data"),
)

private val GROUP_3 = listOf(
    SettingItem(RIcon.Info,      R.string.settings_about,      { it.lightNight }, route = "about"),
    SettingItem(RIcon.Help,      R.string.settings_help,       { it.emerald },    route = "help"),
    SettingItem(RIcon.Sparkles,  R.string.settings_whats_new,  { it.gold },       route = "whats_new"),
    SettingItem(RIcon.Shield,    R.string.settings_privacy,    { it.success },    route = "privacy_policy"),
    SettingItem(RIcon.Document,  R.string.settings_terms,      { it.lightDusk },  route = "terms"),
    SettingItem(RIcon.Mail,      R.string.settings_contact,    { it.lightNight }, route = "contact"),
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
            .rafiqCard()
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
                        .clip(RafiqShape.item)
                        // التظليل على الورق لا على البطاقة: البطاقة داكنة
                        // أصلاً، فتظليلُها يغمقها أكثر ويخنق الأيقونة —
                        // قِيست فهبطت إلى 3.74. وعلى الورق تصير الحاوية
                        // أفتحَ من البطاقة فتُقرأ رقعةً مرفوعة، والأيقونة
                        // فوقها 4.90–7.03.
                        .background(rc.bg)
                        .background(baseCol.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    RafiqIcon(item.icon, 18.dp, baseCol)
                }

                Spacer(Modifier.width(14.dp))

                // Label
                Text(stringResource(item.label),
                    color = rc.ink, modifier = Modifier.weight(1f), style = RafiqType.body)

                // Pro badge
                if (item.badge != null) {
                    Box(
                        Modifier
                            .clip(RafiqShape.item)
                            .background(rc.gold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(item.badge, fontWeight = FontWeight.Bold, color = rc.onGold, style = RafiqType.micro)
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
            RafiqTopBar(
                title  = "الإعدادات",
                onBack = {navController.popBackStack()},
            )

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
