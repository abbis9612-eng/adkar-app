package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/* ══════════════════════════════════════════════════════════════
   تنزيلُ خطوط المصحف — مرّةً واحدةً بإذنٍ صريح

   سبعةٌ وأربعون ملفّاً، نحو تسعةٍ وثمانين ميغابايت. لا تُشحن في
   الحزمة لأنّ التطبيق ٢٥٫٣ فيصير سبعين وما فوق، ولأنّ ترخيصَها لم
   يُحسم بعد.

   ويُنزَّل إلى `filesDir/mushaf` لا إلى الذاكرة الخارجية: ملفٌّ خاصٌّ
   بالتطبيق يُحذف بحذفه، ولا يحتاج إذنَ تخزين.

   وكلُّ ملفٍّ يُنزَّل إلى اسمٍ مؤقّتٍ ثمّ يُعاد تسميتُه — فانقطاعُ
   الشبكة في منتصفه لا يترك خطّاً نصفَ منزَّلٍ يُقرأ فيتعطّل الرسم.
══════════════════════════════════════════════════════════════ */

class MushafDownloader(private val context: Context) {

    /** المصدر — يُغيَّر من مكانٍ واحد إن تبدّل المستودع. */
    private fun urlFor(name: String) =
        "https://raw.githubusercontent.com/MohamadHajjRabee/quran-qcf4/main/fonts/${name}_W.ttf"

    data class Progress(val done: Int, val total: Int, val bytes: Long)

    /**
     * خطُّ صفحةٍ واحدة — نحو مليونَي بايت، ثوانٍ لا دقائق.
     *
     * وهذا ما يجعل الصفحةَ المصحفية تظهر عند أوّل فتحٍ بدل انتظار
     * تسعةٍ وثمانين ميغابايت: كلُّ خطٍّ يخدم ثلاثَ عشرةَ صفحةً وسطياً،
     * فيُجلَب الذي تحتاجه الصفحةُ الحاضرة وحدَه، ويُجلَب ما بعده حين
     * تُقلَب إليه. والتنزيلُ الكامل يبقى خياراً لمن أراد المصحفَ كلَّه
     * دون إنترنت.
     *
     * @return true إن صار الخطُّ على الجهاز.
     */
    suspend fun fetchOne(fonts: MushafFonts, name: String): Boolean = withContext(Dispatchers.IO) {
        val out = fonts.fileFor(name)
        if (out.exists() && out.length() > 0) return@withContext true
        val tmp = File(out.parentFile, "$name.part")
        try {
            val conn = (URL(urlFor(name)).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
            }
            conn.inputStream.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
            }
            tmp.renameTo(out)
        } catch (e: Exception) {
            tmp.delete()
            false
        }
    }

    /**
     * @return null عند التمام، أو رسالةَ الخطأ.
     */
    suspend fun download(
        layout: MushafLayout,
        fonts: MushafFonts,
        onProgress: (Progress) -> Unit,
    ): String? = withContext(Dispatchers.IO) {
        var bytes = 0L
        val total = layout.fonts.size
        layout.fonts.forEachIndexed { i, name ->
            val out = fonts.fileFor(name)
            if (out.exists()) {
                bytes += out.length()
                onProgress(Progress(i + 1, total, bytes))
                return@forEachIndexed
            }
            val tmp = File(out.parentFile, "${name}.part")
            try {
                val conn = (URL(urlFor(name)).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 60_000
                }
                conn.inputStream.use { input ->
                    tmp.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
                }
                if (conn.responseCode !in 200..299) throw IllegalStateException("HTTP ${conn.responseCode}")
                if (!tmp.renameTo(out)) throw IllegalStateException("تعذّر حفظُ $name")
                bytes += out.length()
                onProgress(Progress(i + 1, total, bytes))
            } catch (e: Exception) {
                tmp.delete()
                return@withContext "توقّف التنزيل عند الخطّ ${i + 1} من $total — ${e.message ?: "تعذّر الاتّصال"}"
            }
        }
        null
    }
}
