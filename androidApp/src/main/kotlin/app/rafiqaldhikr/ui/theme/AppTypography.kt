package app.rafiqaldhikr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import app.rafiqaldhikr.R

/**
 * خط الواجهة: Cairo — الأوضح والأكثر أُلفة في التطبيقات العربية الحديثة.
 * خط المصحف: Scheherazade New (SIL) — نسخ قرآني بوزن سميك واضح على الشاشات.
 */
val CairoFamily = FontFamily(
    Font(R.font.cairo_regular,  FontWeight.Normal),
    Font(R.font.cairo_semibold, FontWeight.Medium),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold,     FontWeight.Bold),
)

val QuranFamily = FontFamily(
    Font(R.font.scheherazade_regular,  FontWeight.Normal),
    Font(R.font.scheherazade_semibold, FontWeight.Medium),
    Font(R.font.scheherazade_semibold, FontWeight.SemiBold),
    Font(R.font.scheherazade_bold,     FontWeight.Bold),
)

/**
 * خط الأذكار والأدعية: Noto Naskh Arabic — نسخ حديث مصمم خصيصاً لوضوح
 * النص المشكول على الشاشات؛ الحركات مرتبة لا تتزاحم والحروف كبيرة مريحة.
 */
val NaskhFamily = FontFamily(
    Font(R.font.noto_naskh_regular,  FontWeight.Normal),
    Font(R.font.noto_naskh_semibold, FontWeight.Medium),
    Font(R.font.noto_naskh_semibold, FontWeight.SemiBold),
    Font(R.font.noto_naskh_bold,     FontWeight.Bold),
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

/**
 * خط الأرقام: IBM Plex Arabic — أرقامه واضحة ومتمايزة (٦ و٧ لا تلتبسان)،
 * أنسب للأوقات والعدادات من Cairo. يُستخدم في NumbersStyle.
 */
val NumbersFamily = FontFamily(
    Font(R.font.ibm_plex_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_arabic_medium,  FontWeight.Medium),
    Font(R.font.ibm_plex_arabic_bold,    FontWeight.Bold),
)

val RafiqTypography = Typography(
    displayLarge  = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 30.sp),
    bodyMedium    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 26.sp),
    labelLarge    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)


/**
 * نمط الأرقام الكبيرة (العداد، الأوقات، الإحصائيات):
 * IBM Plex Arabic Bold مع tnum — أرقام واضحة متمايزة بعرض ثابت فلا تهتز أثناء العد.
 */
val NumbersStyle = TextStyle(
    fontFamily = NumbersFamily,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum",
)
