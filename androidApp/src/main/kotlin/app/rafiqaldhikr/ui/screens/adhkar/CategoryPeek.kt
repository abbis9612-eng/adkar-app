package app.rafiqaldhikr.ui.screens.adhkar

import android.content.Context
import org.json.JSONArray

/**
 * إطلالةُ الباب — عددُ أذكاره وأوّلُها بمصدره ودرجته.
 *
 * بطاقةُ «الآن» في شاشة الأبواب تعرض أوّلَ ذكرٍ من الباب الحاضر، فلا
 * يُفتَح البابُ ليُعرَف ما فيه. وهذه تقرؤه من **أصلِ التطبيق نفسِه**
 * (`adhkar_<key>.json`) لا من نسخةٍ في الكود: النصُّ الدينيُّ مصدرُه
 * واحدٌ يُراجَع في موضعٍ واحد، ومن نسخه إلى الكود صار له نسختان
 * تختلفان ولا يُدرى أيُّهما المعتمدة — وهي قاعدةُ [app.rafiqaldhikr.ui.screens.tasbeeh.PrayerCycle]
 * نفسُها.
 *
 * وبـ`org.json` وهي في أندرويد نفسِه: لا حزمةَ جديدةً من أجل خمسةِ
 * ملفّات، ولا شيءَ يحتاج شبكة.
 */
data class CatPeek(
    /** عددُ أذكار الباب كلِّه. */
    val total: Int,
    /** أوّلُ الباب — مشكَّلاً حرفاً بحرفٍ كما في الأصل. */
    val firstText: String,
    val source: String,
    val grade: String,
)

object CategoryPeek {

    /*  `sort_order` لا الفهرس: من رتّب الملفَّ أو أضاف إليه لا يُغيّر
        أوّلَ البابِ المعروضَ من حيث لا يدري. */
    private val cache = HashMap<String, CatPeek?>(8)

    fun of(context: Context, key: String): CatPeek? = synchronized(cache) {
        cache.getOrPut(key) { read(context, key) }
    }

    private fun read(context: Context, key: String): CatPeek? = runCatching {
        val arr = JSONArray(
            context.assets.open("adhkar_$key.json").bufferedReader().use { it.readText() },
        )
        if (arr.length() == 0) return@runCatching null
        var first = arr.getJSONObject(0)
        var firstOrder = first.optString("sort_order").toIntOrNull() ?: Int.MAX_VALUE
        for (i in 1 until arr.length()) {
            val o = arr.getJSONObject(i)
            val order = o.optString("sort_order").toIntOrNull() ?: continue
            if (order < firstOrder) { first = o; firstOrder = order }
        }
        val text = first.optString("text_ar").takeIf { it.isNotBlank() } ?: return@runCatching null
        CatPeek(
            total     = arr.length(),
            firstText = text,
            source    = first.optString("source"),
            grade     = first.optString("source_grade"),
        )
    }.getOrNull()
}
