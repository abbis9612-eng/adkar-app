package app.rafiq.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/*
 * الأثر — أحاديثُ الذكر والدعاء بأدلّتها
 * ════════════════════════════════════
 *
 * ═══ وما الفرقُ بينه وبين الأذكار ═══
 *
 * الأذكارُ نصوصٌ **يقولها** القارئ: «الحمد لله وحده…».
 * والأثرُ أحاديثُها: «مَنْ قَرَأَ بِالْآيَتَيْنِ مِنْ آخِرِ سُورَةِ
 * الْبَقَرَةِ فِي لَيْلَةٍ كَفَتَاهُ» — **دليلُ الذكر وفضلُه، لا الذكرُ
 * نفسُه**.
 *
 * ولو خُلطا لقرأ القارئُ خبراً عن فضلٍ على أنّه ذكرٌ يُقال. فلكلٍّ بابُه،
 * والأثرُ هو جوابُ «من أين لكم هذا؟».
 *
 * ═══ ولماذا يُعرَض الشرحُ والتخريجُ كاملين ═══
 *
 * شروطُ الناشر: «عدم التعديل أو الإضافة أو الحذف». فلا يُقتطع متنٌ ولا
 * يُلخَّص شرحٌ ولا يُختصر تخريج. وما لا يُعرَض كاملاً لا يُعرَض.
 *
 * وتُعرَض نسبةُ المصدر مع كلّ نصّ: اسمُ الناشر وتاريخُ الإصدار — شرطٌ
 * من شروطه، وهو في موضعه: من يقرأ حديثاً يحقُّ له أن يعرف من أين جاء.
 */

/** حديثٌ واحدٌ بإسناده وشرحه. */
data class AtharItem(
    /** مفتاحُ البابِ — تُترجمه الواجهةُ ولا يُعرَض كما هو. */
    val category: String,
    val title: String,
    val titleEn: String,
    val text: String,
    val textEn: String,
    /** النسبةُ ثمّ التخريجُ بالجزء والصفحة والرقم، بسطرٍ فاصلٍ بينهما. */
    val source: String,
    val grade: String,
    /** الشرحُ كما نشره المصدر — لا يُلخَّص. */
    val explanation: String,
    val explanationEn: String,
    /** رقمُه عند المصدر — يُعرَض ليُراجَع عنده. */
    val sourceId: String,
)

/** الملفُّ كلُّه: نسبةُ الناشر ونصوصُه. */
data class AtharBook(
    val publisher: String,
    val publisherUrl: String,
    val fetchedOn: String,
    val items: List<AtharItem>,
)

/** أبوابُ الأثر بترتيب ورودها، وعددُ كلّ باب. */
fun atharCategories(items: List<AtharItem>): List<Pair<String, Int>> {
    val seen = LinkedHashMap<String, Int>()
    for (i in items) seen[i.category] = (seen[i.category] ?: 0) + 1
    return seen.toList()
}

private val ATHAR_JSON = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * يحلّل `athar.json`.
 *
 * @return `null` إن لم يكن الملفُّ موجوداً أو تعذّر تحليلُه — فالأثرُ بابٌ
 *         يُخفى إن لم يُجلب، ولا يُسقط التطبيق.
 */
fun parseAthar(raw: String): AtharBook? {
    val root = runCatching { ATHAR_JSON.parseToJsonElement(raw) }.getOrNull() as? JsonObject
        ?: return null
    val items = (root["items"] as? JsonArray)?.filterIsInstance<JsonObject>() ?: return null
    if (items.isEmpty()) return null

    return AtharBook(
        publisher = root.str("publisher").orEmpty(),
        publisherUrl = root.str("publisher_url").orEmpty(),
        fetchedOn = root.str("fetched_on").orEmpty(),
        items = items.mapNotNull { o ->
            val text = o.str("text_ar") ?: return@mapNotNull null
            val grade = o.str("source_grade") ?: return@mapNotNull null
            val source = o.str("source") ?: return@mapNotNull null
            AtharItem(
                category = o.str("category").orEmpty(),
                title = o.str("title").orEmpty(),
                titleEn = o.str("title_en").orEmpty(),
                text = text,
                textEn = o.str("text_en").orEmpty(),
                source = source,
                grade = grade,
                explanation = o.str("virtue").orEmpty(),
                explanationEn = o.str("virtue_en").orEmpty(),
                sourceId = o.str("hadeeth_id").orEmpty(),
            )
        },
    )
}

private fun JsonObject.str(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
