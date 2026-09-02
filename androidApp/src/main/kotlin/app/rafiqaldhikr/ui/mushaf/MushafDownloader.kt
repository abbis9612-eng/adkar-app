package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/* ══════════════════════════════════════════════════════════════
   تنزيلُ خطوط المصحف — مرّةً واحدةً بإذنٍ صريح

   ثمانيةٌ وأربعون ملفّاً، نحو تسعين ميغابايت. لا تُشحن في
   الحزمة لأنّ التطبيق ٢٥٫٣ فيصير سبعين وما فوق، ولأنّ ترخيصَها لم
   يُحسم بعد.

   ويُنزَّل إلى `filesDir/mushaf` لا إلى الذاكرة الخارجية: ملفٌّ خاصٌّ
   بالتطبيق يُحذف بحذفه، ولا يحتاج إذنَ تخزين.

   وكلُّ ملفٍّ يُنزَّل إلى اسمٍ مؤقّتٍ ثمّ يُعاد تسميتُه — فانقطاعُ
   الشبكة في منتصفه لا يترك خطّاً نصفَ منزَّلٍ يُقرأ فيتعطّل الرسم.
══════════════════════════════════════════════════════════════ */

class MushafDownloader(private val context: Context) {

    /*  المصدر — يُغيَّر من مكانٍ واحد إن تبدّل المستودع.

        وخطوطُ المتن وحدَها تحمل لاحقةَ `_W` في أسمائها؛ أمّا خطُّ لوحِ
        السور فاسمُ ملفِّه `QCF4_QBSML.ttf` مجرّداً. */
    private fun urlFor(name: String): String {
        val file = if (name.startsWith("QCF4_Hafs")) "${name}_W.ttf" else "$name.ttf"
        return "https://raw.githubusercontent.com/MohamadHajjRabee/quran-qcf4/main/fonts/$file"
    }

    data class Progress(val done: Int, val total: Int, val bytes: Long)

    /**
     * خطُّ صفحةٍ واحدة — نحو مليونَي بايت، ثوانٍ لا دقائق.
     *
     * وهذا ما يجعل الصفحةَ المصحفية تظهر عند أوّل فتحٍ بدل انتظار
     * تسعين ميغابايت: كلُّ خطٍّ يخدم ثلاثَ عشرةَ صفحةً وسطياً،
     * فيُجلَب الذي تحتاجه الصفحةُ الحاضرة وحدَه، ويُجلَب ما بعده حين
     * تُقلَب إليه. والتنزيلُ الكامل يبقى خياراً لمن أراد المصحفَ كلَّه
     * دون إنترنت.
     *
     * @return true إن صار الخطُّ على الجهاز.
     */
    suspend fun fetchOne(fonts: MushafFonts, name: String): Boolean = withContext(Dispatchers.IO) {
        val out = fonts.fileFor(name)
        if (isUsableFont(out)) return@withContext true
        // ملفٌّ موجودٌ لكنّه تالف — يُحذف ليُعاد جلبُه بدل أن يبقى للأبد.
        if (out.exists()) out.delete()
        // اسمٌ مؤقّتٌ خاصٌّ بهذا المسار: التنزيلُ الكامل يستعمل غيرَه، وكانا
        // اسماً واحداً فيتسابقان على الملفّ نفسه ويكتب أحدُهما فوق الآخر.
        val tmp = File(out.parentFile, "$name.page.part")
        try {
            download(urlFor(name), tmp)
            if (!isUsableFont(tmp)) throw IllegalStateException("ملفٌّ غيرُ صالح")
            tmp.renameTo(out)
        } catch (e: CancellationException) {
            // إلغاءٌ طبيعيّ (قُلبت صفحةٌ أو غُودرت الشاشة) — لا يُبتلع، ولو
            // بُلع لانكسرت بنيةُ التزامن ولظهر الإلغاءُ للقارئ خطأَ شبكة.
            tmp.delete()
            throw e
        } catch (e: Exception) {
            tmp.delete()
            false
        }
    }

    /**
     * تنزيلٌ إلى ملفٍّ مؤقّت مع التحقّق من الاستجابة وإغلاق الاتّصال.
     *
     * ثلاثةُ أشياء لم تكن هنا: فحصُ رمز الاستجابة قبل الكتابة (فكانت صفحةُ
     * خطأٍ من GitHub تُحفَظ كـ`.ttf`)، و`disconnect()` (فتتراكم مقابسُ
     * الإبقاء عبر ٤٨ جلبةً متتالية)، ومقارنةُ الطول المنزَّل بالمعلَن.
     */
    private fun download(url: String, tmp: File) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            val declared = conn.contentLengthLong
            conn.inputStream.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
            }
            if (declared > 0 && tmp.length() != declared) {
                throw IllegalStateException("انقطع النقل: ${tmp.length()} من $declared")
            }
        } finally {
            conn.disconnect()
        }
    }

    /**
     * أهذا ملفُّ خطٍّ حقيقيّ؟
     *
     * كان الشرطُ `exists() && length() > 0` وحدَه. فأيُّ صفحةِ خطأٍ أو
     * ردٍّ من بوّابةٍ مقيَّدة تُكتب باسم `.ttf`، ويصير الشرطُ صادقاً إلى
     * الأبد: `isPageReady` تقول إنّ الصفحة جاهزة، و`Typeface` يفشل بصمت،
     * فتسقط الصفحةُ إلى النصّ العاديّ ولا سبيل لإصلاحها إلا حذفُ التسعين
     * ميغابايت كلِّها.
     *
     * فيُفحص الطولُ الأدنى وتوقيعُ الملفّ: كلُّ TTF/OTF يبدأ بأربعة بايتات
     * معروفة.
     */
    private fun isUsableFont(f: File): Boolean {
        if (!f.exists() || f.length() < MIN_FONT_BYTES) return false
        return try {
            f.inputStream().use { s ->
                val head = ByteArray(4)
                if (s.read(head) != 4) return false
                val tag = head.joinToString("") { "%02X".format(it) }
                // 00010000 = TrueType · 4F54544F = "OTTO" · 74727565 = "true"
                tag == "00010000" || tag == "4F54544F" || tag == "74727565"
            }
        } catch (e: Exception) {
            false
        }
    }

    /** أصغرُ خطّ لوحٍ نحو ٣٢ ألف بايت؛ فما دون العشرة آلاف ليس خطّاً. */
    private val MIN_FONT_BYTES = 10_000L

    /**
     * @return null عند التمام، أو رسالةَ الخطأ.
     */
    suspend fun download(
        layout: MushafLayout,
        fonts: MushafFonts,
        onProgress: (Progress) -> Unit,
    ): String? = withContext(Dispatchers.IO) {
        // بقايا محاولاتٍ قُطعت — تتراكم في filesDir بلا أن يراها أحد.
        fonts.clearPartials()

        var bytes = 0L
        val total = layout.fonts.size
        layout.fonts.forEachIndexed { i, name ->
            val out = fonts.fileFor(name)
            if (isUsableFont(out)) {
                bytes += out.length()
                onProgress(Progress(i + 1, total, bytes))
                return@forEachIndexed
            }
            if (out.exists()) out.delete()
            val tmp = File(out.parentFile, "$name.all.part")
            try {
                download(urlFor(name), tmp)
                if (!isUsableFont(tmp)) throw IllegalStateException("ملفٌّ غيرُ صالح")
                if (!tmp.renameTo(out)) throw IllegalStateException("تعذّر حفظُ $name")
                bytes += out.length()
                onProgress(Progress(i + 1, total, bytes))
            } catch (e: CancellationException) {
                tmp.delete()
                throw e
            } catch (e: Exception) {
                tmp.delete()
                return@withContext "توقّف التنزيل عند الخطّ ${i + 1} من $total — ${e.message ?: "تعذّر الاتّصال"}"
            }
        }
        null
    }
}
