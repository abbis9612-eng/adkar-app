package app.rafiqaldhikr.ui.screens.arbaeen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.AtharBook
import app.rafiq.domain.model.parseAthar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * الأربعون النوويّة — القراءةُ وتقدّمُها
 * ═══════════════════════════════════
 *
 * ═══ ولماذا التقدّمُ في التفضيلات لا في القاعدة ═══
 *
 * اثنان وأربعون مفتاحاً منطقيّاً لا تستحقّ جدولاً ولا ترحيلاً. وقاعدةُ
 * المشروع أن لا يُغيَّر مخطّطٌ بلا خطّة ترحيلٍ كاملة — فثمنُ جدولٍ هنا
 * أكبرُ من نفعه.
 *
 * وهو تقدّمُ قراءةٍ لا عبادةٌ تُحتسب: من مسح بياناتَ التطبيق فقد علاماتَه
 * ولم يفقد شيئاً يُحاسب عليه.
 */
class ArbaeenViewModel(private val ctx: Context) : ViewModel() {

    private val _book = MutableStateFlow<AtharBook?>(null)

    /** `null` ما لم يُقرأ بعد أو لم يُجلب الملفّ. */
    val book: StateFlow<AtharBook?> = _book.asStateFlow()

    private val _read = MutableStateFlow<Set<Int>>(emptySet())

    /** أرقامُ ما قُرئ — واحدٌ إلى اثنين وأربعين. */
    val read: StateFlow<Set<Int>> = _read.asStateFlow()

    init {
        viewModelScope.launch {
            _book.value = withContext(Dispatchers.IO) {
                runCatching {
                    ctx.assets.open(ASSET).use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()?.let(::parseAthar)
            }
            _read.value = withContext(Dispatchers.IO) { load() }
        }
    }

    fun toggle(n: Int) {
        val next = if (n in _read.value) _read.value - n else _read.value + n
        _read.value = next
        viewModelScope.launch(Dispatchers.IO) { save(n, n in next) }
    }

    private fun prefs() = ctx.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    private fun load(): Set<Int> = runCatching {
        val p = prefs()
        (1..MAX).filterTo(mutableSetOf()) { p.getBoolean(it.toString(), false) }
    }.getOrDefault(emptySet())

    private fun save(n: Int, on: Boolean) {
        runCatching { prefs().edit().putBoolean(n.toString(), on).apply() }
    }

    companion object {
        private const val ASSET = "arbaeen.json"
        private const val STORE = "rafiq_arbaeen"

        /** عددُ الأربعين — واثنان زائدان، وهو عددُها المعروف. */
        const val MAX = 42
    }
}
