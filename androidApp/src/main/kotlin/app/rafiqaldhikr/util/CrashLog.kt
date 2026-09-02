package app.rafiqaldhikr.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/* ══════════════════════════════════════════════════════════════
   سجلُّ الانهيار — لأنّ «يفتح ويُغلق» ليست معلومة

   حين يسقط التطبيقُ على جهاز صاحبِه لا يصل إلينا شيء: لا سجلَّ ولا
   رسالة، فيبقى التشخيصُ تخميناً بين عشرين احتمالاً. وهذا يكتب أثرَ
   الانهيار في ملفٍّ داخل التطبيق، فيعرضه عند الفتح التالي ويستطيع
   صاحبُه إرسالَه بضغطةٍ واحدة.

   والمعالِجُ لا يبتلع شيئاً: يكتب ثمّ يُسلّم إلى معالج النظام كما هو،
   فسلوكُ الانهيار لا يتغيّر — يُوثَّق فقط.
══════════════════════════════════════════════════════════════ */

object CrashLog {

    private const val FILE = "last_crash.txt"

    private fun file(context: Context) = File(context.filesDir, FILE)

    /** يُركَّب أوّلَ شيءٍ في `onCreate` — قبل أيّ عملٍ قد يسقط. */
    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            // الكتابةُ نفسُها محروسة: لو فشلت لا تحجب معالجَ النظام.
            runCatching {
                val trace = StringWriter().also { w ->
                    PrintWriter(w).use { error.printStackTrace(it) }
                }.toString()
                file(context).writeText(
                    buildString {
                        appendLine("الإصدار: ${app.rafiqaldhikr.BuildConfig.VERSION_NAME}")
                        appendLine("أندرويد: ${android.os.Build.VERSION.SDK_INT}")
                        appendLine("الجهاز: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                        appendLine("الخيط: ${thread.name}")
                        appendLine()
                        append(trace)
                    }
                )
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** أثرُ آخرِ انهيار، أو null إن لم يقع شيء. */
    fun read(context: Context): String? = runCatching {
        file(context).takeIf { it.exists() }?.readText()?.takeIf { it.isNotBlank() }
    }.getOrNull()

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }
}
