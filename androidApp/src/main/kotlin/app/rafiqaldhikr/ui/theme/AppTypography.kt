package app.rafiqaldhikr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import app.rafiqaldhikr.R

val IbmPlexArabic = FontFamily(
    Font(R.font.ibm_plex_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_arabic_medium,  FontWeight.Medium),
    Font(R.font.ibm_plex_arabic_bold,    FontWeight.Bold),
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

val UthmaniFamily = FontFamily(
    Font(R.font.kfgqpc_uthmanic_hafs, FontWeight.Normal),
)

val RafiqTypography = Typography(
    displayLarge  = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium    = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp),
    labelLarge    = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = IbmPlexArabic, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)
