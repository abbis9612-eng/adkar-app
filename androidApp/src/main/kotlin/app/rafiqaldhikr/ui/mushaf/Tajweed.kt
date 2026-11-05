package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import app.rafiqaldhikr.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * تلوينُ التجويد
 * ══════════════
 *
 * ═══ البيانات ═══
 *
 * `tarekeldeeb/quran-tajweed-embedded` بترخيص **CC-BY 4.0**، وتخريجُه في
 * `tools/licenses/TAJWEED-DATA.txt`. وهي **لا تُنسخ كما هي**: مواضعُها
 * مواضعُ في نصِّ مصدرها، ونصُّنا يحمل علاماتِ الوقف (ۖ ۛ) فتنزاح.
 *
 * وقِيس الانزياحُ قبل أيّ بناء: **٦٢٨٥ من ١٣٢٥٢** موضعَ همزةِ وصلٍ يقع
 * على حرفٍ آخر، و١٠٨ آيةً تتجاوز فيها المواضعُ طولَ نصّنا. فالنقلُ
 * المباشر يلوّن **حروفاً خاطئةً في كتاب الله** — وذاك أسوأُ من ألّا
 * يكون تلوينٌ أصلاً.
 *
 * فأُعيدت المحاذاةُ في `tools/build_tajweed.py` مرّةً عند البناء، ويفحص
 * الناتجَ معيارٌ لا يحتمل التأويل: كلُّ حكمٍ له حرفٌ معلومٌ (همزةُ الوصل
 * واللامُ الشمسيّةُ والقلقلة) يجب أن يقع عليه. **٩٬٣٦٩** حكماً فُحص،
 * وصفرُ عطب. وما عطب في المصدر نفسِه (٤٥٠) أُسقط ولم يُخمَّن.
 *
 * ═══ وفي النمط المصحفيّ لا تلوين ═══
 *
 * صفحةُ المصحف تُرسم بخطوط QCF: رموزُ صفحةٍ لا حروفَ يونيكود، فلا يُعرف
 * أين الحرفُ من الرمز. فالتلوينُ في **نمط النصّ** وحدَه، ويُقال ذلك
 * صراحةً في الإعدادات ولا يُترك ليكتشفه من بدّل النمطَ فلم يجد شيئاً.
 */

/** حكمٌ على مدًى من حروف الآية. */
data class TajweedSpan(val start: Int, val end: Int, val rule: Int)

/**
 * أسماءُ الأحكام بترتيب الفهرس في الأصل — **لا يُعاد ترتيبُها**.
 * الفهرسُ مكتوبٌ في `assets/tajweed.txt`، فترتيبُها جزءٌ من البيانات.
 */
private val RULE_NAMES = intArrayOf(
    R.string.tj_ghunnah, R.string.tj_idghaam, R.string.tj_idghaam,
    R.string.tj_idghaam, R.string.tj_idghaam, R.string.tj_idghaam,
    R.string.tj_ikhfa, R.string.tj_ikhfa, R.string.tj_iqlab,
    R.string.tj_madd, R.string.tj_madd, R.string.tj_madd,
    R.string.tj_madd, R.string.tj_madd,
    R.string.tj_qalqalah, R.string.tj_wasl, R.string.tj_lam_shams,
    R.string.tj_silent,
)

@StringRes
fun tajweedName(rule: Int): Int = RULE_NAMES.getOrElse(rule) { R.string.tj_ghunnah }

/**
 * لونُ الحكم.
 *
 * وستّةُ ألوانٍ لا ثمانيةَ عشر: الأحكامُ المتقاربةُ تُجمع تحت لونٍ واحد
 * (الإدغامُ بأنواعه الخمسة لونٌ، والمدودُ الخمسةُ لونٌ). وثمانيةَ عشرَ
 * لوناً على صفحةٍ واحدةٍ تُرى زينةً لا دلالة، ولا يحفظها أحد.
 *
 * و[night] يرفع الإضاءة: ألوانُ النهار على ورقٍ داكنٍ تغيب.
 */
fun tajweedColor(rule: Int, night: Boolean): Color = when (rule) {
    0 -> if (night) Color(0xFF7FD69B) else Color(0xFF1E7A44)   // غنّة
    1, 2, 3, 4, 5 -> if (night) Color(0xFF8FB8F0) else Color(0xFF1F4E96) // إدغام
    6, 7 -> if (night) Color(0xFFE0A86A) else Color(0xFF9A5B12)  // إخفاء
    8 -> if (night) Color(0xFFD79BD7) else Color(0xFF7A2A7A)     // إقلاب
    9, 10, 11, 12, 13 -> if (night) Color(0xFFE8757F) else Color(0xFFA81E2C) // مدّ
    14 -> if (night) Color(0xFF6FC9C9) else Color(0xFF0E6E6E)    // قلقلة
    //  همزةُ الوصل واللامُ الشمسيّةُ والساكنُ: **لا تُنطق**، فتُرمَّد
    //  ولا تُلوَّن. واللونُ يقول «انطقه هكذا»، والرمادُ يقول «لا تنطقه».
    else -> if (night) Color(0xFF6E6A62) else Color(0xFF9A948A)
}

/**
 * يقرأ أحكامَ الصفحة من الأصل — مرّةً واحدةً ثمّ يُخزَّن.
 *
 * والملفُّ ٥٦٣ كيلوبايت، ويُقرأ كلُّه دفعةً: قراءتُه سطراً سطراً لكلّ
 * صفحةٍ تفتح الأصلَ ستّمئةِ مرّةٍ في الختمة الواحدة.
 */
object TajweedStore {
    @Volatile private var cache: Map<Int, List<TajweedSpan>>? = null

    suspend fun load(ctx: Context): Map<Int, List<TajweedSpan>> =
        cache ?: withContext(Dispatchers.IO) {
            cache ?: runCatching {
                val map = HashMap<Int, List<TajweedSpan>>(6300)
                ctx.assets.open("tajweed.txt").bufferedReader().forEachLine { line ->
                    if (line.isBlank()) return@forEachLine
                    val p = line.split('|')
                    if (p.size < 3) return@forEachLine
                    val key = p[0].toInt() * 1000 + p[1].toInt()
                    map[key] = p[2].split(';').mapNotNull { sp ->
                        val f = sp.split(',')
                        if (f.size != 3) null
                        else TajweedSpan(f[0].toInt(), f[1].toInt(), f[2].toInt())
                    }
                }
                map
            }.getOrDefault(emptyMap()).also { cache = it }
        }

    /** مفتاحُ الآية في الخريطة. */
    fun key(surah: Int, ayah: Int): Int = surah * 1000 + ayah
}
