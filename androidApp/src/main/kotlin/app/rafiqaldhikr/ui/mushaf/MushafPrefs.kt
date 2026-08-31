package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/* ══════════════════════════════════════════════════════════════
   نمطُ عرض المصحف — أربعةٌ يختار منها صاحبُه

   ويُحفظ في SharedPreferences لا في قاعدة البيانات: تفضيلُ عرضٍ لا
   بيانات، وإضافةُ عمودٍ إلى الجدول تستلزم هجرةً كاملة — وقاعدةُ
   المشروع تمنع تغييرَ البنية بلا خطّة هجرةٍ معروضة.
══════════════════════════════════════════════════════════════ */

enum class MushafMode(val label: String, val note: String) {
    /** ما كان في التطبيق: تصفّحٌ بالسورة وآيةٌ في بطاقة. */
    CLASSIC("قائمةُ الآيات", "آيةٌ في سطرٍ مع رقمها — أسهلُ للبحث والتفسير"),

    /** صفحةٌ بترقيم المصحف، نصٌّ متّصلٌ مضبوطُ الطرفين. */
    PAGE("صفحةٌ مضبوطة", "بترقيم المصحف المدنيّ — يعمل دون إنترنت بلا تنزيل"),

    /** الصفحةُ المصحفية بخطّ QCF4 — خمسةَ عشرَ سطراً كما في الورقة. */
    MUSHAF("الصفحةُ المصحفية", "مطابقةٌ لمصحف المدينة سطراً بسطر — تحتاج تنزيلاً"),

    /** المصحفيةُ بخلفيةٍ داكنة للقراءة الليلية. */
    MUSHAF_NIGHT("المصحفيةُ ليلاً", "الصفحةُ نفسُها بحبرٍ فاتحٍ على ورقٍ داكن"),
    ;

    val needsFonts: Boolean get() = this == MUSHAF || this == MUSHAF_NIGHT
}

class MushafPrefs(context: Context) {
    private val sp = context.getSharedPreferences("rafiq_mushaf", Context.MODE_PRIVATE)

    var mode: MushafMode
        get() = runCatching { MushafMode.valueOf(sp.getString(KEY_MODE, null) ?: "") }
            .getOrDefault(MushafMode.PAGE)
        set(v) = sp.edit().putString(KEY_MODE, v.name).apply()

    /** مقاسُ الخطّ بالنقاط — يُضبط في ورقة الإعدادات. */
    var fontSize: Int
        get() = sp.getInt(KEY_SIZE, 22).coerceIn(16, 34)
        set(v) = sp.edit().putInt(KEY_SIZE, v.coerceIn(16, 34)).apply()

    var lastPage: Int
        get() = sp.getInt(KEY_PAGE, 1).coerceIn(1, 604)
        set(v) = sp.edit().putInt(KEY_PAGE, v.coerceIn(1, 604)).apply()

    private companion object {
        const val KEY_MODE = "mode"
        const val KEY_SIZE = "size"
        const val KEY_PAGE = "page"
    }
}

/** يُبدَّل ليُعاد التركيب حين يتغيّر النمط — كما في [app.rafiqaldhikr.ui.theme.LocalColorTick]. */
val LocalMushafTick = compositionLocalOf { 0 }

@Composable
fun rememberMushafPrefs(): MushafPrefs {
    val ctx = LocalContext.current
    return remember { MushafPrefs(ctx) }
}
