package app.rafiqaldhikr.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/* ═══════════════════════════════════════════════════════════════════
   تخزين اختيار الألوان — SharedPreferences لا قاعدة البيانات

   لونُ العرض حالةُ جهازٍ لا بياناتُ مستخدم: لا يُصدَّر، ولا يُزامَن،
   ولا يعني شيئاً على جهازٍ آخر. ووضعُه في UserPrefs كان يعني عمودين
   جديدين في SQLDelight — أي ترحيلَ مخطَّطٍ يمسّ تقدّم المستخدم
   وسلاسله وعلاماته المرجعية، مقابل تفضيلِ عرضٍ لا غير.

   (وقاعدة AGENTS.md صريحة: لا تغييرَ مخطَّط بلا خطّة ترحيل تُعرض
   أوّلاً. هذا المسار يتجنّب السؤال من أصله.)
═══════════════════════════════════════════════════════════════════ */

private const val FILE   = "rafiq_colors"
private const val PAPER  = "paper"
private const val ACCENT = "accent"

class ColorPrefs(context: Context) {
    private val sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun paper():  Color? = sp.getString(PAPER,  null)?.toColorOrNull()
    fun accent(): Color? = sp.getString(ACCENT, null)?.toColorOrNull()

    fun set(paper: Color?, accent: Color?) {
        sp.edit().apply {
            if (paper  == null) remove(PAPER)  else putString(PAPER,  paper.toHex())
            if (accent == null) remove(ACCENT) else putString(ACCENT, accent.toHex())
            apply()
        }
    }

    fun clear() = sp.edit().clear().apply()
}

fun Color.toHex(): String = "%08X".format(value.shr(32).toInt())

private fun String.toColorOrNull(): Color? =
    runCatching { Color(toLong(16)) }.getOrNull()

/** يُعاد تركيبه عند كل تغيير فتنتشر الألوان في التطبيق كلّه فوراً. */
val LocalColorTick = compositionLocalOf { 0 }

@Composable
fun rememberColorPrefs(): ColorPrefs {
    val ctx = LocalContext.current
    return remember(ctx) { ColorPrefs(ctx.applicationContext) }
}
