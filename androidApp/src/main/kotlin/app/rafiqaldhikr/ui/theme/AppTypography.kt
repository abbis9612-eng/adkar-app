package app.rafiqaldhikr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import app.rafiqaldhikr.R

/**
 * Cairo — the app's primary UI face: modern, rounded, and far friendlier
 * for Arabic interfaces than IBM Plex. Real static weights 400–700.
 */
val CairoFamily = FontFamily(
    Font(R.font.cairo_regular,  FontWeight.Normal),
    Font(R.font.cairo_medium,   FontWeight.Medium),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold,     FontWeight.Bold),
    Font(R.font.cairo_bold,     FontWeight.ExtraBold),
)

/** Kept for backwards compatibility — now points at Cairo. */
val IbmPlexArabic = CairoFamily

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

val UthmaniFamily = FontFamily(
    Font(R.font.kfgqpc_uthmanic_hafs, FontWeight.Normal),
)

/**
 * Type scale tuned for Arabic (Cairo).
 * Arabic script needs generous line heights — every style declares one
 * so ascenders/descenders and diacritics never clip.
 *
 * Quranic verses & adhkar body text should use [AmiriFamily] /
 * [UthmaniFamily] directly for a calligraphic feel.
 */
val RafiqTypography = Typography(
    displayLarge   = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 44.sp),
    displayMedium  = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 40.sp),
    displaySmall   = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 24.sp, lineHeight = 34.sp),

    headlineLarge  = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 24.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 21.sp, lineHeight = 30.sp),
    headlineSmall  = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 28.sp),

    titleLarge     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Bold,   fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge      = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall      = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),

    labelLarge     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = CairoFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
)
