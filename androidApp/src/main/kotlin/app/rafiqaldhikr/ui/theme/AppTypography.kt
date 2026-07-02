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

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

val UthmaniFamily = FontFamily(
    Font(R.font.kfgqpc_uthmanic_hafs, FontWeight.Normal),
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
