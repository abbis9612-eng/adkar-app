package app.rafiqaldhikr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R

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
