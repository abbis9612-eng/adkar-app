package app.rafiq.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/*
 * جَرْدُ الإسناد
 * ════════════
 *
 * ═══ ولماذا هي خندقٌ لا شاشةَ «عن التطبيق» ═══
 *
 * كلُّ تطبيقٍ يقول «مصادرُنا موثوقة». وهذه الشاشةُ **تُثبت**: عددُ النصوص،
 * وكم منها بتخريج، وكلُّ سطرٍ يُلمَس فيُعرَض مصدرُه ودرجتُه.
 *
 * ولا يستطيع أحدٌ أن يضعها إلّا إن كانت نسبتُه مئةً. فمن فيه نصٌّ بلا
 * إسنادٍ **تفضحه شاشتُه** — فلا توضَع. وهذا بعينه ما يجعلها خندقاً: ليست
 * ميزةً تُنسَخ، بل نتيجةُ عملٍ لا يُنسَخ.
 *
 * ═══ وقاعدةُ الجمع هي قاعدةُ الحارس نفسُها ═══
 *
 * `tools/check_religious_sources.py` يفحص **كلَّ** ملفّات `assets` ذات اللاحقة `json` وفيها
 * `text_ar` (إلّا القرآنَ والتفسيرَ وبيانات السور: لها مسارُها وحجمُها،
 * وليست نصوصاً مفردةً بمصدرٍ لكلٍّ منها).
 *
 * وهذه الشاشةُ تستكشف بالقاعدة نفسِها لا بقائمةٍ مكتوبة. **ولولا ذلك
 * لانزاحت عن الحارس**: يُضاف ملفُّ محتوًى جديدٌ فيفحصه الحارسُ ولا تعرفه
 * الشاشةُ، فتقول «مئةٌ بالمئة» وهي لا ترى كلَّ شيء. وشاشةٌ تُثبت صدقاً
 * **لا يصحّ أن تكذب هي نفسُها**.
 */

/** ملفّاتٌ ليست نصوصاً مفردةً بمصدرٍ لكلٍّ منها — توأمُ `SKIP` في الحارس. */
val ISNAD_SKIP = setOf(
    "quran_uthmani.json",
    "tafsir_muyassar.json",
    "surah_metadata.json",
)

/** نصٌّ واحدٌ وإسنادُه. */
data class IsnadEntry(
    val text: String,
    val source: String,
    val grade: String,
    /** الملفُّ الذي جاء منه بلا لاحقة — تُترجمه الواجهة. */
    val group: String,
)

/** خلاصةُ الجرد. */
data class IsnadSummary(
    val total: Int,
    /** كم نصّاً له مصدرٌ **ودرجةٌ** غيرُ فارغَين. */
    val sourced: Int,
    /** الدرجاتُ مرتّبةً من الأكثر — لا بترتيبٍ مكتوبٍ هنا. */
    val byGrade: List<Pair<String, Int>>,
    /** الأبوابُ مرتّبةً من الأكثر. */
    val byGroup: List<Pair<String, Int>>,
)

/**
 * النسبةُ المئويّةُ **قسمةً صحيحةً لا تقريباً لأعلى**.
 *
 * ‎٩٩٫٦٪‎ تُعرَض ٩٩ لا ١٠٠: الشاشةُ تدّعي الكمال، فلو قرّبت لأعلى لادّعته
 * وهي ناقصة — ونصٌّ واحدٌ بلا إسنادٍ يكفي لإسقاط الدعوى كلِّها.
 */
fun isnadPercent(summary: IsnadSummary): Int =
    if (summary.total == 0) 0 else summary.sourced * 100 / summary.total

/** يبني الخلاصةَ — دالّةٌ خالصةٌ تُختبر بلا جهاز. */
fun summarizeIsnad(entries: List<IsnadEntry>): IsnadSummary = IsnadSummary(
    total = entries.size,
    sourced = entries.count { it.source.isNotBlank() && it.grade.isNotBlank() },
    byGrade = entries
        .filter { it.grade.isNotBlank() }
        .groupingBy { it.grade }.eachCount()
        .toList().sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first }),
    byGroup = entries
        .groupingBy { it.group }.eachCount()
        .toList().sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first }),
)

private val ISNAD_JSON = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * يحلّل ملفَّ محتوًى واحداً.
 *
 * وموضعُه هنا لا في `androidApp`: التحليلُ بـ`kotlinx.serialization` وهي
 * في هذه الوحدة، **وإضافتُها إلى وحدة الواجهة لتحليلٍ يصحّ هنا زيادةٌ لا
 * تُطلب**. وفي هذه الوحدة يُختبر كذلك بلا جهاز.
 *
 * @param fileName اسمُ الملفّ بلاحقته — يصير [IsnadEntry.group] بلا لاحقة
 * @param raw نصُّ الملفّ
 * @return فارغةٌ إن لم يكن ملفَّ نصوصٍ أو تعذّر تحليلُه — ولا يُرمى
 *         استثناءٌ: ملفُّ إعداداتٍ في الأصول ليس عطباً
 */
fun parseIsnadFile(fileName: String, raw: String): List<IsnadEntry> {
    if (!fileName.endsWith(".json") || fileName in ISNAD_SKIP) return emptyList()
    val root = runCatching { ISNAD_JSON.parseToJsonElement(raw) }.getOrNull() ?: return emptyList()

    //  الملفُّ قائمةٌ أو كائنٌ قيمُه قوائم — كلتا الصورتين في الأصول
    val objects: List<JsonObject> = when (root) {
        is JsonArray -> root.filterIsInstance<JsonObject>()
        is JsonObject -> root.values
            .filterIsInstance<JsonArray>()
            .flatten()
            .filterIsInstance<JsonObject>()
        else -> emptyList()
    }

    val group = fileName.removeSuffix(".json")
    return objects.mapNotNull { o ->
        val text = o.str("text_ar") ?: return@mapNotNull null
        IsnadEntry(
            text = text,
            source = o.str("source").orEmpty(),
            grade = o.str("source_grade").orEmpty(),
            group = group,
        )
    }
}

private fun JsonObject.str(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
