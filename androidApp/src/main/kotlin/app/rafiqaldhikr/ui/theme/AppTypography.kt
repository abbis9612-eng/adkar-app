package app.rafiqaldhikr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import app.rafiqaldhikr.R

/**
 * خط الواجهة الموحّد: Noto Sans Arabic — خط عربي اعتيادي واضح ومألوف،
 * يُستخدم لكل نصوص الواجهة والأرقام. (استبدل Cairo وIBM Plex).
 */
val UiFamily = FontFamily(
    Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_sans_arabic_medium,  FontWeight.Medium),
    Font(R.font.noto_sans_arabic_medium,  FontWeight.SemiBold),
    Font(R.font.noto_sans_arabic_bold,    FontWeight.Bold),
)

/** خط المصحف: Scheherazade New — نسخ قرآني واضح على الشاشات. */
val QuranFamily = FontFamily(
    Font(R.font.scheherazade_regular,  FontWeight.Normal),
    Font(R.font.scheherazade_semibold, FontWeight.Medium),
    Font(R.font.scheherazade_semibold, FontWeight.SemiBold),
    Font(R.font.scheherazade_bold,     FontWeight.Bold),
)

/**
 * خط الأذكار والأدعية المشكولة: Noto Naskh Arabic — الحركات مرتبة لا تتزاحم.
 * (يبقى مخصصاً للنص المشكول فقط، وباقي الواجهة على UiFamily).
 */
val NaskhFamily = FontFamily(
    Font(R.font.noto_naskh_regular,  FontWeight.Normal),
    Font(R.font.noto_naskh_semibold, FontWeight.Medium),
    Font(R.font.noto_naskh_semibold, FontWeight.SemiBold),
    Font(R.font.noto_naskh_bold,     FontWeight.Bold),
)

/** خط البسملة الزخرفي: Amiri — لعبارة البسملة فقط (نص قرآني قصير). */
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

val RafiqTypography = Typography(
    displayLarge  = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium    = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp),
    labelLarge    = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)

/**
 * نمط الأرقام (العدّادات، الأوقات، الإحصائيات): نفس خط الواجهة الاعتيادي
 * مع tnum — أرقام بعرض ثابت فلا تهتز أثناء العدّ.
 */
val NumbersStyle = TextStyle(
    fontFamily = UiFamily,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum",
)
