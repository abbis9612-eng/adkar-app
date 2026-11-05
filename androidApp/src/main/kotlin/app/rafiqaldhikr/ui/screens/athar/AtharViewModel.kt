package app.rafiqaldhikr.ui.screens.athar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.AtharBook
import app.rafiq.domain.model.atharCategories
import app.rafiq.domain.model.parseAthar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** الأثر — يُقرأ من الأصول مرّةً، وتُرشَّح أبوابُه في الذاكرة. */
class AtharViewModel(private val ctx: Context) : ViewModel() {

    private val _book = MutableStateFlow<AtharBook?>(null)

    /** `null` ما لم يُقرأ بعد أو لم يُجلب الملفّ. */
    val book: StateFlow<AtharBook?> = _book.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)

    /** البابُ المختار — و`null` تعني الكلّ. */
    val category: StateFlow<String?> = _category.asStateFlow()

    init {
        viewModelScope.launch {
            _book.value = withContext(Dispatchers.IO) {
                runCatching {
                    ctx.assets.open("athar.json").use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()?.let(::parseAthar)
            }
        }
    }

    fun pick(key: String?) {
        _category.value = if (_category.value == key) null else key
    }

    /** أبوابُ الكتابِ وأعدادُها. */
    fun categories() = _book.value?.items?.let(::atharCategories).orEmpty()
}
