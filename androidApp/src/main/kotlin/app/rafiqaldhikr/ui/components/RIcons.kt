package app.rafiqaldhikr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * مجموعة الأيقونات الاحترافية الموحّدة (Lucide) — مصدر الحقيقة الوحيد لأيقونات
 * الواجهة. كلها stroke على شبكة 24، بأطراف دائرية، تُلوَّن عبر tint فتتكيّف مع
 * الثيم. تُعرض عبر [RafiqIcon].
 */
enum class RIcon(@DrawableRes val res: Int) {
    // تنقّل وأدوات
    ChevronLeft(R.drawable.ic_chevron_left),
    ChevronRight(R.drawable.ic_chevron_right),
    ArrowLeft(R.drawable.ic_arrow_left),
    ArrowRight(R.drawable.ic_arrow_right),
    Close(R.drawable.ic_close),
    Plus(R.drawable.ic_plus),
    Settings(R.drawable.ic_settings),
    Search(R.drawable.ic_search),
    Share(R.drawable.ic_share),
    Copy(R.drawable.ic_copy),
    Edit(R.drawable.ic_edit),
    Refresh(R.drawable.ic_refresh),
    Trash(R.drawable.ic_trash),
    Send(R.drawable.ic_send),
    Download(R.drawable.ic_download),
    Upload(R.drawable.ic_upload),
    More(R.drawable.ic_more),
    // وسائط
    Play(R.drawable.ic_play),
    Pause(R.drawable.ic_pause),
    SkipForward(R.drawable.ic_skip_forward),
    SkipBack(R.drawable.ic_skip_back),
    // نظام واتصال
    Bell(R.drawable.ic_bell),
    Mail(R.drawable.ic_mail),
    Info(R.drawable.ic_info),
    Help(R.drawable.ic_help),
    Warning(R.drawable.ic_warning),
    Alert(R.drawable.ic_alert),
    Inbox(R.drawable.ic_inbox),
    Shield(R.drawable.ic_shield),
    Document(R.drawable.ic_document),
    Globe(R.drawable.ic_globe),
    Palette(R.drawable.ic_palette),
    Font(R.drawable.ic_font),
    Widget(R.drawable.ic_widget),
    Clock(R.drawable.ic_clock),
    Pin(R.drawable.ic_pin),
    Sparkles(R.drawable.ic_sparkles),
    // مجال
    Home(R.drawable.ic_home),
    User(R.drawable.ic_user),
    Book(R.drawable.ic_book),
    Heart(R.drawable.ic_heart),
    Bookmark(R.drawable.ic_bookmark),
    Compass(R.drawable.ic_compass),
    Flame(R.drawable.ic_flame),
    Trophy(R.drawable.ic_trophy),
    Medal(R.drawable.ic_medal),
    Crown(R.drawable.ic_crown),
    Star(R.drawable.ic_star),
    Bulb(R.drawable.ic_bulb),
    Food(R.drawable.ic_food),
    Health(R.drawable.ic_health),
    Check(R.drawable.ic_check),
    CheckDouble(R.drawable.ic_check_double),
    // أجرام سماوية / وقت
    Sun(R.drawable.ic_sun),
    Sunrise(R.drawable.ic_sunrise),
    Sunset(R.drawable.ic_sunset),
    Moon(R.drawable.ic_moon),
    Duha(R.drawable.ic_duha),
}

/** يعرض أيقونة موحّدة بحجم ولون محددين (اللون افتراضياً لون النص). */
@Composable
fun RafiqIcon(
    icon: RIcon,
    size: Dp = 24.dp,
    tint: Color = LocalRafiqColors.current.ink,
) {
    Icon(
        painter = painterResource(icon.res),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size),
    )
}
