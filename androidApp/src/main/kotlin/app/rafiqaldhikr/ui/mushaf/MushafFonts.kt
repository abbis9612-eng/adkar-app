package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import java.io.File

/* ══════════════════════════════════════════════════════════════
   خطوطُ المصحف — لماذا لا تُشحن في الحزمة

   خطُّ QCF4 سبعةٌ وأربعون ملفّاً للمتن (بدل ٦٠٤ في الإصدارين الأوّل
   والثاني)، وثامنٌ وأربعون للوح السور. ومجموعُها بصيغة TTF نحو تسعين
   ميغابايت — قِيست بالتنزيل لا بالتقدير. والتطبيقُ اليوم ٢٥٫٣، فشحنُها
   يجعله سبعين وما فوق.

   ولا تُطلب الثمانيةُ والأربعون لصفحةٍ واحدة: خمسُ مئةٍ وستُّ صفحاتٍ من
   الستّ مئة والأربع يكفيها خطٌّ واحد، وخمسٌ وتسعون تحتاج ثلاثةً لأنّ
   فيها لوحَ سورةٍ وبسملة.

   ولذلك تُنزَّل مرّةً واحدةً بإذنٍ صريح، وتُحفظ في `filesDir`. وقاعدةُ
   «يعمل دون إنترنت» لا تُكسر: نمطُ الصفحة المضبوطة يعمل دائماً بخطٍّ
   مشحونٍ في الحزمة، والمصحفيُّ إضافةٌ فوقه لا شرطٌ له.

   وثمّة ما يُحسم قبل أيّ شحن: ترخيصُ الخطوط. مستودعُ المصدر يقول
   «احترم شروطَ الاستعمال الأصلية» ولا يذكر ترخيصاً صريحاً، وخطوطُ
   مجمع الملك فهد لها شروطُها. فلا يُدخَل في التطبيق ما لم يُتحقّق منه.
══════════════════════════════════════════════════════════════ */

/** خطوطُ صفحةٍ واحدة: المتنُ ولوحُ السورة والبسملة. */
data class PageFonts(
    val body: FontFamily,
    val plate: FontFamily?,
    val bism: FontFamily?,
)

class MushafFonts(private val context: Context) {

    private val dir: File get() = File(context.filesDir, "mushaf").apply { mkdirs() }

    private val cache = HashMap<String, FontFamily>()

    /** هل نُزِّلت الخطوطُ كلُّها؟ */
    fun isReady(layout: MushafLayout): Boolean =
        layout.fonts.all { File(dir, "$it.ttf").exists() }

    /** هل تكتمل هذه الصفحةُ رسماً — خطُّها وخطُّ لوحِها وبسملتِها؟ */
    fun isPageReady(layout: MushafLayout, page: Int): Boolean =
        layout.fontsNeeded(page).all { File(dir, "$it.ttf").exists() }

    fun downloadedCount(layout: MushafLayout): Int =
        layout.fonts.count { File(dir, "$it.ttf").exists() }

    fun fileFor(name: String): File = File(dir, "$name.ttf")

    /** عائلةُ خطٍّ باسمه، أو null إن لم يُنزَّل بعد. */
    fun family(name: String?): FontFamily? {
        if (name == null) return null
        cache[name]?.let { return it }
        val f = fileFor(name)
        if (!f.exists()) return null
        return runCatching { FontFamily(Font(f)) }.getOrNull()?.also { cache[name] = it }
    }

    /** عائلةُ خطِّ صفحةٍ ما، أو null إن لم تُنزَّل بعد. */
    fun familyFor(layout: MushafLayout, page: Int): FontFamily? =
        family(layout.fontOf(page))

    /**
     * الخطوطُ الثلاثةُ التي تُرسَم بها الصفحة — أو null إن نقص أحدُها.
     *
     * وتُطلب مجتمعةً لا فرادى: لو رُسم اللوحُ بخطّ الصفحة لخرج كلمةً
     * أخرى، فالنقصُ يعني نصّاً خاطئاً لا فراغاً — وهذا لا يُعرض.
     */
    fun pageFonts(layout: MushafLayout, page: Int): PageFonts? {
        val body = family(layout.fontOf(page)) ?: return null
        val pg = layout.page(page) ?: return null
        val types = pg.t.toSet()
        val plate = if (GlyphType.SURAH_HEADER in types) {
            family(layout.plateFont) ?: return null
        } else {
            null
        }
        val bism = if (GlyphType.BISMILLAH in types) {
            family(layout.bismFont) ?: return null
        } else {
            null
        }
        return PageFonts(body, plate, bism)
    }

    /** حجمُ ما نُزِّل بالميغابايت — يُعرض في الإعدادات. */
    fun sizeMb(): Double =
        (dir.listFiles()?.sumOf { it.length() } ?: 0L) / 1_048_576.0

    fun clear() {
        dir.listFiles()?.forEach { it.delete() }
        cache.clear()
    }
}
