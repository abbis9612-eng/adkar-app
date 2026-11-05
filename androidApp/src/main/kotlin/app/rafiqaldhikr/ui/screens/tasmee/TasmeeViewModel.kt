package app.rafiqaldhikr.ui.screens.tasmee

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.TasmeeResult
import app.rafiq.domain.model.Verdict
import app.rafiq.domain.model.tasmee
import app.rafiq.domain.repository.QuranRepository
import app.rafiqaldhikr.ui.tasmee.AudioRecorder
import app.rafiqaldhikr.ui.tasmee.MAX_RECORD_SECONDS
import app.rafiqaldhikr.ui.tasmee.TasmeeRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * حالاتُ التسميع
 * ═════════════
 *
 * ═══ ولماذا [Stage.Unheard] حالةٌ قائمةٌ بنفسها ═══
 *
 * لو كانت الحالاتُ «يسجّل» و«أتمّ» و«أخطأ» لاضطُرّت الشاشةُ أن تختار بين
 * الثناء واللوم. و«لم أتبيّن» ليست لوماً مخفَّفاً: هي **إقرارٌ بأنّ الآلةَ
 * لم تسمع**، ويُطلب فيها أن يعيد. فجُعلت حالةً تُعرَض بلونها ونصِّها، لا
 * نتيجةً ناقصةً تُحشى في قالب الخطأ.
 */
class TasmeeViewModel(
    private val ctx: Context,
    private val quran: QuranRepository,
) : ViewModel() {

    sealed interface Stage {
        /** ينتظر أن يبدأ. */
        data object Idle : Stage

        /** يسجّل — و[seconds] تُعرَض ليعرف أنّ الحدَّ يقترب. */
        data class Recording(val seconds: Float) : Stage

        /** يُنصِت — والنموذجُ يأخذ ثانيةً أو ثانيتين. */
        data object Thinking : Stage

        /** حكمٌ على التلاوة. */
        data class Judged(val result: TasmeeResult, val heard: String) : Stage

        /** لم تُسمع — ويُطلب أن يعيد. */
        data class Unheard(val heard: String) : Stage

        /** تعذّر الميكروفون — بلا إذنٍ أو مشغولٌ بنداء. */
        data object NoMic : Stage

        /** النموذجُ غيرُ مشحونٍ في هذه النسخة. */
        data object NoModel : Stage
    }

    private val _stage = MutableStateFlow<Stage>(Stage.Idle)
    val stage: StateFlow<Stage> = _stage.asStateFlow()

    private val _ayah = MutableStateFlow("")

    /** نصُّ الآية بالرسم العثمانيّ — يُعرَض ويُقابَل. */
    val ayah: StateFlow<String> = _ayah.asStateFlow()

    private val recorder = AudioRecorder()
    private var recognizer: TasmeeRecognizer? = null
    private var loaded = false

    fun load(surah: Int, ayahNumber: Int) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            _ayah.value = quran.getAyah(surah, ayahNumber)?.textUthmani.orEmpty()
        }
    }

    /** يبدأ التسجيلَ، أو يُنهيه إن كان جارياً. */
    fun toggle() {
        if (recorder.recording) {
            finish()
            return
        }
        if (!recorder.start()) {
            _stage.value = Stage.NoMic
            return
        }
        _stage.value = Stage.Recording(0f)
        viewModelScope.launch {
            //  القراءةُ على خيطٍ خلفيّ: `read` تحجب حتى يصل الصوت.
            withContext(Dispatchers.IO) {
                while (recorder.recording && recorder.drain()) {
                    //  لا شيءَ هنا — `drain` هي الانتظار
                }
            }
            //  بلغ الحدَّ وهو يقرأ: يُقطع ويُحكم على ما سُمع.
            if (recorder.recording) finish()
        }
        viewModelScope.launch {
            while (recorder.recording) {
                _stage.value = Stage.Recording(recorder.seconds)
                delay(100)
            }
        }
    }

    private fun finish() {
        val audio = recorder.stop()
        if (audio.isEmpty()) {
            _stage.value = Stage.Idle
            return
        }
        _stage.value = Stage.Thinking
        viewModelScope.launch {
            val text = _ayah.value
            val heard = withContext(Dispatchers.Default) {
                val r = recognizer ?: TasmeeRecognizer.createOrNull(ctx)?.also { recognizer = it }
                r?.listen(audio)
            }
            if (heard == null) {
                _stage.value = Stage.NoModel
                return@launch
            }
            val result = withContext(Dispatchers.Default) {
                tasmee(text, heard.text, heard.confidence)
            }
            _stage.value =
                if (result.verdict == Verdict.UNSURE) Stage.Unheard(heard.text)
                else Stage.Judged(result, heard.text)
        }
    }

    fun again() {
        _stage.value = Stage.Idle
    }

    /** هل أصولُ النموذج مشحونةٌ في هذه النسخة؟ */
    fun modelPresent(): Boolean =
        runCatching { ctx.assets.open("tasmee/MODEL.json").close(); true }
            .getOrDefault(false)

    override fun onCleared() {
        //  `viewModelScope` يُلغى **قبل** هذه الدالّة، فلا يُنتظَر فيها
        //  شيءٌ — تُغلَق الموارد فقط.
        runCatching { recorder.stop() }
        runCatching { recognizer?.close() }
        recognizer = null
    }

    companion object {
        /** يُعرَض في الشاشة ليعرف القارئُ حدَّ النافذة. */
        const val LIMIT_SECONDS = MAX_RECORD_SECONDS
    }
}
