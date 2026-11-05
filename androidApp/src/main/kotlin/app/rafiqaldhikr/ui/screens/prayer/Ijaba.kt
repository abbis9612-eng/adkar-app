package app.rafiqaldhikr.ui.screens.prayer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.rafiq.domain.model.AtharBook
import app.rafiq.domain.model.parseAthar

/* ═══════════════════════════════════════════════════════════════════
   إجابةُ المؤذّن — من حديث عمر رضي الله عنه في صحيح مسلم (٣٨٥)

   الجُمَلُ السبعُ تُقتطع من متن الحديث نفسِه بترتيبها، ولا تُكتب في
   الكود: «إِذَا قَالَ الْمُؤَذِّنُ: … فَقَالَ أَحَدُكُمُ: …، ثُمَّ قَالَ: …،
   قَالَ: …». فما يقوله المؤذّنُ وما يُجاب به كلاهما بلفظ الحديث.

   ولا يُضاف ما ليس فيه: «الصلاةُ خيرٌ من النوم» في أذان الفجر ليست في
   هذا الحديث، فلا خطوةَ لها — قاعدةُ المشروع ألّا يُكتب نصٌّ دينيٌّ
   بلا مصدرٍ في أصول التطبيق.

   ويُعرض تحتها حديثُ عبد الله بن عمرو (مسلم ٣٨٤) كاملاً: «…ثُمَّ صَلُّوا
   عَلَيَّ… ثُمَّ سَلُوا اللهَ لِيَ الْوَسِيلَةَ…».
═══════════════════════════════════════════════════════════════════ */

/** جملةُ المؤذّن وما يُجاب به. */
data class IjabaPair(val muezzin: String, val reply: String)

/** نصٌّ مرويٌّ كاملاً بمصدره ودرجته. */
data class Riwaya(val text: String, val source: String)

data class Ijaba(val pairs: List<IjabaPair>, val hadith: Riwaya, val after: Riwaya)

/** معرّفا الحديثين في `athar.json`. */
private const val HADITH_UMAR = "65086"
private const val HADITH_IBN_AMR = "65087"

/*  المطابقةُ بالحروف لا بالتشكيل.
 *
 *  ترتيبُ الحركات على الحرف الواحد يختلف بين نصٍّ ونصّ: «ثُمَّ» في الملفّ
 *  شدّةٌ ثمّ فتحة، وقد تُكتب فتحةً ثمّ شدّة — يُريان واحداً ولا يتطابقان.
 *  فكانت الإجابةُ تُبنى فارغةً بصمت. والآن يُطابَق هيكلُ الحروف وتُقبل
 *  بينها أيُّ حركاتٍ بأيّ ترتيب، والنصُّ المعروضُ يبقى كما في الملفّ. */
private const val MARKS = "[\\u064B-\\u065F\\u0670]*"
private fun loose(bare: String): String = bare.map { if (it == ' ') "\\s+" else Regex.escape(it.toString()) + MARKS }.joinToString("")

private val TURN = Regex("،?\\s*(?:${loose("إذا قال المؤذن")}|${loose("ثم قال")}):\\s*")
private val REPLY = Regex("،\\s*(?:${loose("فقال أحدكم")}|${loose("قال")}):\\s*")
private val TAIL = Regex("\\s*${loose("من قلبه")}.*$")

/**
 * يقتطع أزواجَ (المؤذّن، الجواب) من متن الحديث — دالّةٌ محضةٌ تُختبر.
 *
 * ما بعد «« » وحدَه هو كلامُ النبيّ ﷺ، فيُبدأ منه. وآخرُ جوابٍ يُقطع
 * عند «مِنْ قَلْبِهِ»: هو تمامُ الحديث («…مِنْ قَلْبِهِ دَخَلَ الْجَنَّةَ»)
 * لا من لفظ الجواب. وأيُّ قطعةٍ لا تنقسم إلى اثنين تُسقط الكلَّ: لفظٌ
 * مقتطَعٌ خطأً لا يُعرض على أنّه جوابُ المؤذّن.
 */
internal fun ijabaPairs(text: String): List<IjabaPair> {
    val start = text.indexOf('«')
    if (start < 0) return emptyList()
    val pairs = text.substring(start + 1)
        .split(TURN)
        .filter { it.isNotBlank() }
        .map { part ->
            val bits = part.split(REPLY)
            if (bits.size != 2) return emptyList()
            IjabaPair(bits[0].trim(), bits[1].replace(TAIL, "").trimEnd('»', '.', ' '))
        }
    return pairs
}

object IjabaStore {
    @Volatile private var cached: Ijaba? = null

    /** يُبنى من `parseAthar` — المحلِّلُ نفسُه الذي تقرأ به شاشةُ «الأثر» الملفَّ. */
    suspend fun load(ctx: Context): Ijaba? = cached ?: withContext(Dispatchers.IO) {
        cached ?: runCatching {
            fromBook(parseAthar(ctx.assets.open("athar.json").bufferedReader().use { it.readText() }))
        }.getOrNull()?.also { cached = it }
    }
}

/** من كتاب الأثر إلى الإجابة — أو `null` إن نقص شيء، فلا تُعرض نصفُ إجابة. */
internal fun fromBook(book: AtharBook?): Ijaba? {
    val items = book?.items ?: return null
    fun riwaya(id: String) = items.firstOrNull { it.sourceId == id }?.let {
        Riwaya(it.text, it.source.lineSequence().first().trim() + " · " + it.grade)
    }
    val umar = riwaya(HADITH_UMAR) ?: return null
    val after = riwaya(HADITH_IBN_AMR) ?: return null
    val pairs = ijabaPairs(umar.text)
    return if (pairs.size == 7) Ijaba(pairs, umar, after) else null
}
