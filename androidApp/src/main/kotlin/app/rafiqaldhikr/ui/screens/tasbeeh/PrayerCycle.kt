package app.rafiqaldhikr.ui.screens.tasbeeh

import android.content.Context
import org.json.JSONArray

/**
 * دورةُ دُبُر الصلاة — ثلاثٌ وثلاثون وثلاثٌ وثلاثون وأربعٌ وثلاثون، ثمّ
 * تمامُ المائة.
 *
 * ونصوصُها وأعدادُها ومصادرُها **تُقرأ من `adhkar_prayer.json`** لا تُكتب
 * في الكود: فالنصُّ الدينيُّ مصدرٌ واحدٌ يُراجَع في موضعٍ واحد، ومن نسخه
 * إلى الكود صار له نسختان تختلفان ولا يدري أحدٌ أيُّهما المعتمدة.
 *
 * وتُقرأ بـ`org.json` — وهي في أندرويد نفسِه — كما يفعل [app.rafiqaldhikr.ui.mushaf.SurahNames]،
 * فلا حزمةَ جديدةً من أجل ملفٍّ واحد، ولا شيءَ يحتاج شبكة.
 */
data class CycleStep(
    /** النصُّ المشكَّلُ كما في الأصل حرفاً بحرف. */
    val text: String,
    val count: Int,
    val source: String,
    val grade: String,
    val virtue: String?,
)

object PrayerCycle {

    /*  `sort_order` في الأصل: ٣ تسبيحٌ · ٤ حمدٌ · ٥ تكبيرٌ · ٦ تمامُ المائة.
        والمفاتيحُ لا الفهارسُ: من رتّب الملفَّ أو أضاف إليه لا يُحرّك هذه. */
    private const val ASSET = "adhkar_prayer.json"
    private val CYCLE_ORDERS = listOf(3, 4, 5)
    private const val HUNDREDTH_ORDER = 6

    @Volatile private var cycleCache: List<CycleStep>? = null
    @Volatile private var hundredthCache: CycleStep? = null
    @Volatile private var loaded = false

    /** خطواتُ الدورة الثلاث بترتيبها. */
    fun steps(context: Context): List<CycleStep> {
        ensure(context)
        return cycleCache.orEmpty()
    }

    /** تمامُ المائة — «لا إله إلا الله وحده…» مرّةً واحدة. */
    fun hundredth(context: Context): CycleStep? {
        ensure(context)
        return hundredthCache
    }

    private fun ensure(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val byOrder = read(context)
            cycleCache = CYCLE_ORDERS.mapNotNull { byOrder[it] }
            hundredthCache = byOrder[HUNDREDTH_ORDER]
            loaded = true
        }
    }

    private fun read(context: Context): Map<Int, CycleStep> = runCatching {
        val arr = JSONArray(
            context.assets.open(ASSET).bufferedReader().use { it.readText() },
        )
        val out = HashMap<Int, CycleStep>(8)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val order = o.optString("sort_order").toIntOrNull() ?: continue
            val count = o.optString("count").toIntOrNull() ?: continue
            out[order] = CycleStep(
                text   = o.optString("text_ar"),
                count  = count,
                source = o.optString("source"),
                grade  = o.optString("source_grade"),
                virtue = o.optString("virtue").takeIf { it.isNotBlank() },
            )
        }
        out
    }.getOrDefault(emptyMap())
}

/* ══════════════════════════════════════════════════════════════
 *  حسابُ الدورة — محضٌ بلا أندرويد، فيُختبَر
 * ══════════════════════════════════════════════════════════════ */

/**
 * مجموعُ ما عُدّ من المائة: ما تمّ من الخطوات الماضية + ما عُدّ في الحاضرة.
 *
 * وهو موضعُ الخطأ بواحد: من جمع الخطوةَ الحاضرةَ كاملةً قال «٣٣ من ١٠٠»
 * وهو في أوّل التسبيح، ومن نسي الماضياتِ قال «١ من ١٠٠» وهو في التكبير.
 */
fun cycleTotal(step: Int, doneInStep: Int, counts: List<Int>): Int {
    if (counts.isEmpty()) return doneInStep.coerceAtLeast(0)
    val s = step.coerceIn(0, counts.size - 1)
    var sum = 0
    for (i in 0 until s) sum += counts[i]
    return sum + doneInStep.coerceIn(0, counts[s])
}

/** هل بلغت الدورةُ تمامَها عند هذه الخطوة وهذا العدد؟ */
fun cycleComplete(step: Int, doneInStep: Int, counts: List<Int>): Boolean =
    counts.isNotEmpty() && step >= counts.size - 1 && doneInStep >= counts.last()

/** الخطوةُ التالية، أو `null` إن كانت هذه آخرَها. */
fun nextCycleStep(step: Int, size: Int): Int? =
    if (size <= 0 || step >= size - 1) null else step + 1
