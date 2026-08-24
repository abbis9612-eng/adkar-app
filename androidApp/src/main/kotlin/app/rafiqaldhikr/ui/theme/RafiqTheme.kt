package app.rafiqaldhikr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.rafiqaldhikr.R
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LightRafiqPalette
import app.rafiqaldhikr.ui.theme.DarkRafiqPalette

private val LightScheme = lightColorScheme(
    primary              = LightRafiqPalette.emerald,
    onPrimary            = LightRafiqPalette.onEmerald,
    primaryContainer     = LightRafiqPalette.emeraldPastel,
    onPrimaryContainer   = LightRafiqPalette.emerald,
    secondary            = LightRafiqPalette.gold,
    onSecondary          = LightRafiqPalette.onGold,
    secondaryContainer   = LightRafiqPalette.gold.copy(alpha = 0.1f),
    onSecondaryContainer = LightRafiqPalette.gold,
    tertiary             = LightRafiqPalette.gold,
    onTertiary           = LightRafiqPalette.onGold,
    tertiaryContainer    = LightRafiqPalette.meccanBg,
    onTertiaryContainer  = LightRafiqPalette.gold,
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
    onPrimary            = DarkRafiqPalette.onEmerald,
    primaryContainer     = DarkRafiqPalette.emeraldPastel,
    onPrimaryContainer   = DarkRafiqPalette.emerald,
    secondary            = DarkRafiqPalette.gold,
    onSecondary          = DarkRafiqPalette.onGold,
    secondaryContainer   = DarkRafiqPalette.gold.copy(alpha = 0.15f),
    onSecondaryContainer = DarkRafiqPalette.gold,
    tertiary             = DarkRafiqPalette.gold,
    onTertiary           = DarkRafiqPalette.onGold,
    tertiaryContainer    = DarkRafiqPalette.meccanBg,
    onTertiaryContainer  = DarkRafiqPalette.gold,
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

    /*  اختيارُ المستخدم يُطبَّق على اللوحة كلّها لا على الرئيسية وحدها:
     *  يكفي لونان (الورق واللهجة) ويُشتقّ الباقي في [tuned] — فيبقى كل
     *  نصٍّ في التطبيق مقروءاً مهما اختار. و٢٢٥ تركيبة يحرسها اختبار.  */
    @Suppress("UNUSED_EXPRESSION") LocalColorTick.current   // إعادة تركيبٍ عند التغيير
    val prefs = rememberColorPrefs()
    val rafiqPalette = (if (darkTheme) DarkRafiqPalette else LightRafiqPalette)
        .tuned(prefs.paper(), prefs.accent())

    /*  الاتجاه يتبع لغة التطبيق — لا لغة الجهاز.
     *
     *  كان هنا تعليقٌ يقول هذا بالضبط، ولا كودَ يفعله: الاستيرادان
     *  LocalLayoutDirection وLayoutDirection موجودان منذ البداية بلا
     *  استعمال. فكان Compose يشتقّ الاتجاه من لغة النظام وحدها — ومن
     *  فتح التطبيق بالعربية على هاتفٍ لغتُه تركية أو إنجليزية رأى
     *  التطبيق كلّه معكوساً: العلامة يساراً، والأبواب مقلوبة، وكل
     *  start/end في التطبيق على غير موضعه.
     *
     *  والاشتقاق من AppCompatDelegate.getApplicationLocales() جُرِّب وفشل:
     *  القيمة قد تكون فارغة لحظةَ أوّل تركيب فيسقط إلى لغة الجهاز. أمّا
     *  R.bool.is_rtl فيتبع مجلّد الموارد الذي اختاره أندرويد فعلاً لتحميل
     *  النصوص — فلا يفترق الاتجاه عن اللغة المعروضة أبداً.
     */
    val direction =
        if (LocalContext.current.resources.getBoolean(R.bool.is_rtl)) LayoutDirection.Rtl
        else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalRafiqColors    provides rafiqPalette,
        LocalLayoutDirection provides direction,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = RafiqTypography,
            shapes      = RafiqShapes,
        ) {
            // فرض خط الواجهة وارتفاع السطر العربي على كل نص لا يحدّدهما صراحةً.
            //
            // نقل الخط وحده (كما كان) ترك lineHeight = Unspecified، فكان النص
            // يرجع لمقاييس الخط (~1.3) — أي أن نص التطبيق العربي كلّه كان مضغوطاً
            // عمودياً بينما يحتاج 1.7–1.85.
            //
            // القيمة بوحدة em فتتناسب مع أي مقاس، وTrimmedLeading يمنعها من
            // نفخ ارتفاع التسميات ذات السطر الواحد.
            ProvideTextStyle(
                LocalTextStyle.current.copy(
                    fontFamily      = UiFamily,
                    lineHeight      = ArabicLineHeight,
                    lineHeightStyle = TrimmedLeading,
                )
            ) {
                content()
            }
        }
    }
}
