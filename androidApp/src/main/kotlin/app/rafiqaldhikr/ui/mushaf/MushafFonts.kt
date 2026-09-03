package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import java.io.File

/* ══════════════════════════════════════════════════════════════
   خطوطُ المصحف — تُشحن في التطبيق

   **هذا عكسُ ما كان.** كانت تُنزَّل من الإنترنت بإذنٍ صريح، والحجّةُ
   مكتوبةٌ هنا: تسعون ميغابايت تُضاعف حجمَ التطبيق. وقد جُرّب التنزيلُ
   فلم يصل: بين شبكةٍ محدودة، وحوارٍ يسدّ الشاشة، وأثرٍ ينسحب على
   `layout == null` فلا يعود — والصفحةُ المصحفيةُ لا تظهر لصاحبها.

   **والحلُّ الذي لا يمكن أن يفشل هو ألّا يكون ثمّة تنزيلٌ إطلاقاً.**

   فالثمانيةُ والأربعون في `assets/mushaf/`: سبعةٌ وأربعون للمتن وثامنٌ
   وأربعون للوح السور. ٩٢٫٥ ميغابايت خاماً، تنضغط في الحزمة إلى ≈٥٥ —
   فيصير التطبيق نحو ٦٢ ميغابايت. وهو نصفُ «المصحف الذهبي» (١٤٣).

   والمقابل: الصفحةُ المصحفية تُرسم **من أوّل ثانية**، بلا إنترنتٍ ولا
   إذنٍ ولا انتظار. ولا شيءَ فيها يمكن أن يفشل.

   > **وقيدٌ أُصرّح به:** مستودعُ المصدر يمنع إعادةَ النشر صراحةً، ورخصةُ
   > مجمَّع الملك فهد نفسُها تُجيز النسخَ والتوزيع مجاناً. وقد أُخبر
   > صاحبُ التطبيق بهذا التعارض واختار الشحن. والنصُّ الكامل للرخصة في
   > `tools/licenses/KFGQPC-FONT-LICENSE.txt`، والعزوُ في شاشة «حول».

   ولا تُطلب الثمانيةُ والأربعون لصفحةٍ واحدة: خمسُ مئةٍ وستُّ صفحاتٍ من
   الستّ مئة والأربع يكفيها خطٌّ واحد، وخمسٌ وتسعون تحتاج ثلاثةً لأنّ
   فيها لوحَ سورةٍ وبسملة. فالتحميلُ كسولٌ عند أوّل استعمال.
══════════════════════════════════════════════════════════════ */

/** خطوطُ صفحةٍ واحدة: المتنُ ولوحُ السورة والبسملة. */
data class PageFonts(
    val body: FontFamily,
    val plate: FontFamily?,
    val bism: FontFamily?,
)

class MushafFonts(private val context: Context) {

    /*  يُنشأ مرّةً واحدة.
     *
     *  كان `get() = File(...).apply { mkdirs() }` — أي نداءَ نظامٍ لإنشاء
     *  المجلَّد عند **كل قراءةٍ للخاصيّة**. و`isReady` وحدَها تقرؤها ٤٨
     *  مرّة، و`isPageReady` تُنادى داخل التأليف عند كل إعادة تركيب. أي
     *  عشراتُ نداءاتِ القرص على الخيط الرئيسي في كل إطار.  */
    private val dir: File = File(context.filesDir, "mushaf").apply { mkdirs() }

    /*  خريطةٌ متزامنة: الخطوطُ تُهيَّأ مسبقاً على خيط الإدخال/الإخراج
        وتُقرأ من خيط التأليف، فالخريطةُ العاديّة تتسابق عليها. */
    private val cache = java.util.concurrent.ConcurrentHashMap<String, FontFamily>()

    /*  مشحونةٌ في الحزمة — فالجوابُ نعم دائماً، ولا قرصَ يُسأل.
     *
     *  وكانت هذه الدوالُّ تفحص وجودَ ٤٨ ملفّاً على القرص، و`isPageReady`
     *  تُنادى **داخل التأليف** عند كل إعادة تركيب. فسقط معها عملُ قرصٍ
     *  في كل إطار. */
    fun isReady(layout: MushafLayout): Boolean = true

    fun isPageReady(layout: MushafLayout, page: Int): Boolean = true

    fun downloadedCount(layout: MushafLayout): Int = layout.fonts.size

    fun fileFor(name: String): File = File(dir, "$name.ttf")

    /**
     * عائلةُ خطٍّ باسمه.
     *
     * تُقرأ من أصول التطبيق أوّلاً. ويبقى القرصُ بديلاً لمن نزّلها
     * بنسخةٍ سابقة: ملفّاتُه موجودةٌ في `filesDir` ولا معنى لتجاهلها
     * ثمّ حذفها — تُستعمل كما هي حتى يُفرغها من أراد.
     */
    fun family(name: String?): FontFamily? {
        if (name == null) return null
        cache[name]?.let { return it }
        val fam = runCatching { FontFamily(Font("mushaf/$name.ttf", context.assets)) }
            .getOrNull()
            ?: fileFor(name).takeIf { it.exists() }
                ?.let { runCatching { FontFamily(Font(it)) }.getOrNull() }
        return fam?.also { cache[name] = it }
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

    /**
     * يحذف بقايا التنزيلات المقطوعة.
     *
     * كلُّ تنزيلٍ يُلغى في منتصفه يترك `.part` في `filesDir` — لا يراه
     * المستخدم ولا يُحسب في «حجم ما نُزِّل»، ويبقى إلى أن يُحذف التطبيق.
     */
    fun clearPartials() {
        dir.listFiles()?.forEach { if (it.name.endsWith(".part")) it.delete() }
    }

    /** حجمُ الخطوط الصالحة وحدَها — لا البقايا. */
    fun sizeMbOfFonts(): Double =
        (dir.listFiles()?.filter { it.name.endsWith(".ttf") }?.sumOf { it.length() } ?: 0L) / 1_048_576.0
}
