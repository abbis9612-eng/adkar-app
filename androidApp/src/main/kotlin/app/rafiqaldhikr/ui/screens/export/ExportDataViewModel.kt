package app.rafiqaldhikr.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.rafiq.domain.repository.ImportResult
import app.rafiq.domain.repository.UserDataRepository
import kotlinx.coroutines.launch

class ExportDataViewModel(
    private val userDataRepo: UserDataRepository
) : ViewModel() {

    /**
     * يكتب نصَّ التصدير إلى ملفٍّ **على خيط الإدخال والإخراج**، ثمّ
     * يُرجع مُعرّفَه على الخيط الرئيسيّ لتُبنى به نيّةُ المشاركة.
     *
     * كانت الكتابةُ تقع في لامبدا النقر — أي على الخيط الرئيسيّ —
     * وبياناتُ سنةٍ من الاستعمال ليست صغيرة.
     */
    fun writeThenShare(
        write: () -> android.net.Uri,
        onReady: (android.net.Uri) -> Unit,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) { runCatching(write).getOrNull() }
            if (uri == null) onError() else onReady(uri)
        }
    }

    fun exportJson(onReady: (String) -> Unit, onError: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { userDataRepo.exportAsJson() }
                .onSuccess(onReady)
                // كان `onSuccess` وحدَه: يفشل التصديرُ فلا يعلم أحد.
                .onFailure { onError() }
        }
    }

    /**
     * يستورد ملفَّ تصديرٍ سابق.
     *
     * كان التصديرُ يعمل ولا استيرادَ معه — أي نسخٌ احتياطيٌّ بلا استعادة.
     */
    /**
     * يقرأ ملفَّ الاستيراد **على خيط الإدخال والإخراج** ثمّ يستورده.
     *
     * كانت الشاشةُ تقرؤه بـ`readText()` كاملاً على **الخيط الرئيسيّ**
     * داخل ردِّ منتقي الملفّات، والمنتقي كان يسمح بأيّ نوعِ ملفّ —
     * فمن اختار ملفّاً كبيراً جُمّد عنده التطبيقُ حتى ينتهي القراءةُ،
     * أو نفدت الذاكرةُ فانهار.
     *
     * وسقفُ [MAX_IMPORT_BYTES] حارسٌ ثانٍ: ملفُّ تصديرٍ لهذا التطبيق لا
     * يبلغ ثُمنَه، وما تجاوزه ليس ملفَّنا — فيُرفض قبل أن يُحمَّل في
     * الذاكرة لا بعدَه.
     */
    fun importStream(open: () -> java.io.InputStream?, onDone: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    open()?.use { input ->
                        val buf = ByteArray(MAX_IMPORT_BYTES + 1)
                        var n = 0
                        while (n < buf.size) {
                            val r = input.read(buf, n, buf.size - n)
                            if (r < 0) break
                            n += r
                        }
                        //  تجاوز السقف: لا يُحوَّل إلى نصٍّ أصلاً
                        if (n > MAX_IMPORT_BYTES) null
                        else String(buf, 0, n, Charsets.UTF_8)
                    }
                }.getOrNull()
            }
            if (text == null) {
                onDone(ImportResult.Invalid(ImportResult.Reason.NOT_JSON))
                return@launch
            }
            importJson(text, onDone)
        }
    }

    fun importJson(text: String, onDone: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val r = runCatching { userDataRepo.importFromJson(text) }
                .getOrElse { ImportResult.Invalid(ImportResult.Reason.NOT_JSON) }
            onDone(r)
        }
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { userDataRepo.clearAllUserData() }
                .onSuccess { onDone() }
        }
    }
}

/** ثمانيةُ ميغابايت — أضعافُ أكبرِ ملفِّ تصديرٍ يُنتجه هذا التطبيق. */
private const val MAX_IMPORT_BYTES = 8 * 1024 * 1024
