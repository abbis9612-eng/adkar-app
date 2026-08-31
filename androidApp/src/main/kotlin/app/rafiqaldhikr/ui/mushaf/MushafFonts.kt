package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import java.io.File

/* ══════════════════════════════════════════════════════════════
   خطوطُ المصحف — لماذا لا تُشحن في الحزمة

   خطُّ QCF4 سبعةٌ وأربعون ملفّاً (بدل ٦٠٤ في الإصدارين الأوّل والثاني)،
   ومجموعُها بصيغة TTF نحو تسعةٍ وثمانين ميغابايت — قِيست بالتنزيل لا
   بالتقدير. والتطبيقُ اليوم ٢٥٫٣، فشحنُها يجعله سبعين وما فوق.

   ولذلك تُنزَّل مرّةً واحدةً بإذنٍ صريح، وتُحفظ في `filesDir`. وقاعدةُ
   «يعمل دون إنترنت» لا تُكسر: نمطُ الصفحة المضبوطة يعمل دائماً بخطٍّ
   مشحونٍ في الحزمة، والمصحفيُّ إضافةٌ فوقه لا شرطٌ له.

   وثمّة ما يُحسم قبل أيّ شحن: ترخيصُ الخطوط. مستودعُ المصدر يقول
   «احترم شروطَ الاستعمال الأصلية» ولا يذكر ترخيصاً صريحاً، وخطوطُ
   مجمع الملك فهد لها شروطُها. فلا يُدخَل في التطبيق ما لم يُتحقّق منه.
══════════════════════════════════════════════════════════════ */

class MushafFonts(private val context: Context) {

    private val dir: File get() = File(context.filesDir, "mushaf").apply { mkdirs() }

    private val cache = HashMap<String, FontFamily>()

    /** هل نُزِّلت الخطوطُ كلُّها؟ */
    fun isReady(layout: MushafLayout): Boolean =
        layout.fonts.all { File(dir, "$it.ttf").exists() }

    fun downloadedCount(layout: MushafLayout): Int =
        layout.fonts.count { File(dir, "$it.ttf").exists() }

    fun fileFor(name: String): File = File(dir, "$name.ttf")

    /** عائلةُ خطِّ صفحةٍ ما، أو null إن لم تُنزَّل بعد. */
    fun familyFor(layout: MushafLayout, page: Int): FontFamily? {
        val name = layout.fontOf(page) ?: return null
        cache[name]?.let { return it }
        val f = fileFor(name)
        if (!f.exists()) return null
        return runCatching { FontFamily(Font(f)) }.getOrNull()?.also { cache[name] = it }
    }

    /** حجمُ ما نُزِّل بالميغابايت — يُعرض في الإعدادات. */
    fun sizeMb(): Double =
        (dir.listFiles()?.sumOf { it.length() } ?: 0L) / 1_048_576.0

    fun clear() {
        dir.listFiles()?.forEach { it.delete() }
        cache.clear()
    }
}
