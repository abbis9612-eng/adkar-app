package app.rafiqaldhikr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LightRafiqPalette
import app.rafiqaldhikr.ui.theme.DarkRafiqPalette

private val LightScheme = lightColorScheme(
    primary              = LightRafiqPalette.emerald,
    onPrimary            = Color.White,
    primaryContainer     = LightRafiqPalette.emeraldPastel,
    onPrimaryContainer   = LightRafiqPalette.emerald,
    secondary            = LightRafiqPalette.gold,
    onSecondary          = Color.White,
    secondaryContainer   = LightRafiqPalette.gold.copy(alpha = 0.1f),
    onSecondaryContainer = LightRafiqPalette.gold,
    tertiary             = LightRafiqPalette.brownAccent,
    onTertiary           = Color.White,
    tertiaryContainer    = LightRafiqPalette.meccanBg,
    onTertiaryContainer  = LightRafiqPalette.brownAccent,
    surface              = LightRafiqPalette.bg,
    onSurface            = LightRafiqPalette.inkDark,
    surfaceVariant       = LightRafiqPalette.card,
    onSurfaceVariant     = LightRafiqPalette.inkMed,
    outline              = LightRafiqPalette.divider,
    error                = Color(0xFFB3261E),
    background           = LightRafiqPalette.bg,
    onBackground         = LightRafiqPalette.inkDark,
)

private val DarkScheme = darkColorScheme(
    primary              = DarkRafiqPalette.emerald,
    onPrimary            = Color.White,
    primaryContainer     = DarkRafiqPalette.emeraldPastel,
    onPrimaryContainer   = DarkRafiqPalette.emerald,
    secondary            = DarkRafiqPalette.gold,
    onSecondary          = Color.White,
    secondaryContainer   = DarkRafiqPalette.gold.copy(alpha = 0.15f),
    onSecondaryContainer = DarkRafiqPalette.gold,
    tertiary             = DarkRafiqPalette.brownAccent,
    onTertiary           = Color.White,
    tertiaryContainer    = DarkRafiqPalette.meccanBg,
    onTertiaryContainer  = DarkRafiqPalette.brownAccent,
    surface              = DarkRafiqPalette.bg,
    onSurface            = DarkRafiqPalette.inkDark,
    surfaceVariant       = DarkRafiqPalette.card,
    onSurfaceVariant     = DarkRafiqPalette.inkMed,
    outline              = DarkRafiqPalette.divider,
    error                = Color(0xFFF2B8B5),
    background           = DarkRafiqPalette.bg,
    onBackground         = DarkRafiqPalette.inkDark,
)

@Composable
fun RafiqTheme(
    darkTheme:    Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else      -> LightScheme
    }

    val rafiqPalette = if (darkTheme) DarkRafiqPalette else LightRafiqPalette

    // الاتجاه يتبع لغة التطبيق (RTL للعربية، LTR للإنجليزية) بدل فرض RTL دائماً
    CompositionLocalProvider(
        LocalRafiqColors provides rafiqPalette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = RafiqTypography,
            shapes      = RafiqShapes,
            content     = content
        )
    }
}
