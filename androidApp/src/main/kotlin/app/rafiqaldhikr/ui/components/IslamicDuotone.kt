package app.rafiqaldhikr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * مجموعة الأيقونات الإسلامية الاحترافية — أسلوب ثنائي اللون (خطّ زمرّدي + تعبئة ذهبية).
 * ألوانها مضمّنة داخل الرسم (لا تُلوَّن بـ tint)، ولها نسخة داكنة تلقائية في drawable-night.
 * تُعرض عبر [DuotoneIcon].
 */
enum class IcoDuo(@DrawableRes val res: Int) {
    Mosque(R.drawable.ic_isl_mosque),
    Crescent(R.drawable.ic_isl_crescent),
    Star8(R.drawable.ic_isl_star8),
    Tasbeeh(R.drawable.ic_isl_tasbeeh),
    Quran(R.drawable.ic_isl_quran),
    Qibla(R.drawable.ic_isl_qibla),
    Kaaba(R.drawable.ic_isl_kaaba),
    Lantern(R.drawable.ic_isl_lantern),
    Mihrab(R.drawable.ic_isl_mihrab),
    Sun(R.drawable.ic_isl_sun),
    Sunset(R.drawable.ic_isl_sunset),
}

/** يعرض أيقونة إسلامية ثنائية اللون بحجم محدد (بألوانها المضمّنة، بلا tint). */
@Composable
fun DuotoneIcon(icon: IcoDuo, size: Dp = 28.dp) {
    Image(
        painter = painterResource(icon.res),
        contentDescription = null,
        modifier = Modifier.size(size),
    )
}

/** أيقونة محطة اليوم الاحترافية (مصمتة، قابلة للتلوين) حسب معرّف المحطة. */
@androidx.annotation.DrawableRes
fun stationDrawable(id: String): Int = when (id) {
    "wake"          -> R.drawable.ic_st_sunhorizon
    "fajr_morning"  -> R.drawable.ic_st_sun
    "duha"          -> R.drawable.ic_st_sun
    "dhuhr"         -> R.drawable.ic_st_mosque
    "asr_evening"   -> R.drawable.ic_st_star
    "maghrib"       -> R.drawable.ic_st_sunhorizon
    "isha"          -> R.drawable.ic_st_moonstars
    "sleep"         -> R.drawable.ic_st_moon
    "friday_kahf"   -> R.drawable.ic_st_book
    else            -> R.drawable.ic_st_dua
}

/** شارة تصنيف نظيفة بأسلوب آبل: دائرة زمرّدية متدرّجة + أيقونة بيضاء احترافية. */
@Composable
fun CategoryBadge(@DrawableRes iconRes: Int, size: Dp = 54.dp) {
    Box(
        Modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color(0xFF23976E),
                        androidx.compose.ui.graphics.Color(0xFF0C5E43),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(size * 0.50f),
        )
    }
}

/** خريطة تصنيفات الأذكار → أيقونة احترافية. */
@DrawableRes
fun adhkarCatDrawable(key: String): Int = when (key) {
    "morning"   -> R.drawable.ic_sun
    "evening"   -> R.drawable.ic_sunset
    "sleep"     -> R.drawable.ic_moon
    "istighfar" -> R.drawable.ic_st_dua
    "prayer"    -> R.drawable.ic_st_mosque
    else        -> R.drawable.ic_star
}

/** خريطة تصنيفات الأدعية → أيقونة احترافية. */
@DrawableRes
fun duaCatDrawable(key: String): Int = when (key) {
    "morning"    -> R.drawable.ic_sun
    "sleep"      -> R.drawable.ic_moon
    "quran"      -> R.drawable.ic_st_book
    "food"       -> R.drawable.ic_food
    "travel"     -> R.drawable.ic_compass
    "anxiety"    -> R.drawable.ic_heart
    "sickness"   -> R.drawable.ic_health
    "istikharah" -> R.drawable.ic_star
    else         -> R.drawable.ic_st_dua
}

/** أيقونة المسبحة الاحترافية (للشريط السفلي وغيره). */
@Composable
fun MisbahaIcon(size: Dp = 24.dp, tint: androidx.compose.ui.graphics.Color = LocalRafiqColors.current.emerald) {
    androidx.compose.material3.Icon(
        painter = painterResource(R.drawable.ic_st_tasbeeh),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

/** أيقونة المحطة فقط، بلون محدد (للسياقات التي توفّر خلفيتها). */
@Composable
fun StationGlyph(id: String, size: Dp = 26.dp, tint: androidx.compose.ui.graphics.Color = LocalRafiqColors.current.emerald) {
    androidx.compose.material3.Icon(
        painter = painterResource(stationDrawable(id)),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

/**
 * بلاطة محطة بأسلوب آبل: مربّع فاتح بزوايا ناعمة + أيقونة زمرّدية مصمتة.
 * واضحة وسادة بعمق خفيف، تظهر بوضوح على أي خلفية (فاتحة أو داكنة/صورة).
 */
@Composable
fun StationTile(id: String, size: Dp = 56.dp) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.30f))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.22f), RoundedCornerShape(size * 0.30f)),
        contentAlignment = Alignment.Center,
    ) {
        StationGlyph(id, size * 0.54f, rc.emerald)
    }
}
