package app.rafiqaldhikr.ui.screens.isnad

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.ISNAD_SKIP
import app.rafiq.domain.model.IsnadEntry
import app.rafiq.domain.model.parseIsnadFile
import app.rafiq.domain.model.IsnadSummary
import app.rafiq.domain.model.summarizeIsnad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * جَرْدُ الإسناد — جمعُ النصوص
 * ═════════════════════════
 *
 * ═══ يُقرأ من الأصول لا من القاعدة ═══
 *
 * نصوصُ التطبيق ليست كلُّها في قاعدة البيانات: الأذكارُ والأدعيةُ تُبذَر
 * فيها، و`nida.json` يقرؤها `ui/hero/Nida.kt` من الأصول مباشرةً،
 * و`wisdom.json` يقرؤه `WisdomRepositoryImpl` كذلك. فجردٌ من القاعدة
 * يُسقط سبعةَ نصوصٍ وأربعةً — ويقول «مئةٌ بالمئة» وهو لم يرَ أحدَ عشر.
 *
 * والأصولُ هي **المصدرُ الواحد** الذي يفحصه الحارسُ أيضاً. فمنها يُقرأ.
 *
 * ═══ وبقاعدةِ الاستكشاف لا بقائمةٍ مكتوبة ═══
 *
 * يُستعرض كلُّ ملفِّ `json` في الأصول، ويُقبل منه كلُّ كائنٍ فيه
 * `text_ar` — وهو شرطُ الحارس حرفاً بحرف. فملفُّ محتوًى جديدٌ يدخل
 * الجردَ بلا تعديلِ سطرٍ هنا، ولا تنزاح الشاشةُ عن الحارس.
 */
class IsnadViewModel(private val ctx: Context) : ViewModel() {

    private val _entries = MutableStateFlow<List<IsnadEntry>>(emptyList())
    val entries: StateFlow<List<IsnadEntry>> = _entries.asStateFlow()

    private val _summary = MutableStateFlow<IsnadSummary?>(null)

    /** `null` ما لم يُقرأ الجردُ بعد. */
    val summary: StateFlow<IsnadSummary?> = _summary.asStateFlow()

    init {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) { collect() }
            _entries.value = rows
            _summary.value = summarizeIsnad(rows)
        }
    }

    private fun collect(): List<IsnadEntry> {
        val names = runCatching { ctx.assets.list("")?.toList() }.getOrNull().orEmpty()
        val out = ArrayList<IsnadEntry>(256)
        for (name in names.sorted()) {
            //  `assets.list` يُرجع المجلّدات أيضاً (`mushaf` و`tasmee`)،
            //  وفتحُ مجلّدٍ يرمي. فيُفلتر الاسمُ قبل أن يُفتح.
            //  والمتجاوَزُ يُستبعَد **قبل القراءة** لا بعدها: قراءةُ
            //  `quran_uthmani.json` و`tafsir_muyassar.json` نحو عشرين
            //  ميغابايت تُقرأ ثمّ تُرمى في كلّ فتحةٍ للشاشة.
            if (!name.endsWith(".json") || name in ISNAD_SKIP) continue
            val raw = runCatching {
                ctx.assets.open(name).use { it.readBytes().toString(Charsets.UTF_8) }
            }.getOrNull() ?: continue
            out += parseIsnadFile(name, raw)
        }
        return out
    }
}
