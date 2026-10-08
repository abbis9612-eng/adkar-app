package app.rafiqaldhikr.ui.mushaf

import androidx.lifecycle.ViewModel
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.repository.QuranRepository
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.LastReadPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/** آياتُ صفحةٍ بترقيم المصحف — للنمط المضبوط الذي يعمل بلا تنزيل. */
class MushafPageViewModel(
    private val repo: QuranRepository,
    private val khatma: app.rafiq.domain.repository.KhatmaRepository,
) : ViewModel() {

    fun pageFlow(page: Int): Flow<List<AyahInfo>> = repo.getAyahsByPage(page)

    /**
     * آيةٌ بعينها بسورتها ورقمها.
     *
     * وكانت تُلتقط من **صفحة التخطيط** الحاضرة: `getAyahsByPage(page).first()
     * .firstOrNull { … }`. والقاعدةُ تخزّن للآية صفحتَها هي، وهما يفترقان في
     * ٥٦ آيةً على ٢٥ صفحة — كلُّ آيةٍ تبدأ في صفحةٍ وتُتِمّ في التي بعدها
     * (‏`5:77` تُرسم في ١٢٠ وفي القاعدة ١٢١). فكانت الورقةُ تفتح فارغةً على
     * تلك الصفحات، والنسخُ والمشاركةُ يُخرجان العنوانَ بلا نصّ.
     */
    suspend fun ayah(surah: Int, ayah: Int): AyahInfo? = repo.getAyah(surah, ayah)

    /** نصُّ البسملة من القاعدة — الفاتحة ١. لا يُكتب نصٌّ قرآنيّ في الكود. */
    suspend fun basmala(): String? = repo.getAyah(1, 1)?.textUthmani

    suspend fun tafsir(surah: Int, ayah: Int): String? = repo.getTafsir(surah, ayah)

    suspend fun isMarked(surah: Int, ayah: Int): Boolean = repo.isBookmarked(surah, ayah)

    suspend fun note(surah: Int, ayah: Int): String? = repo.ayahNote(surah, ayah)

    suspend fun setNote(surah: Int, ayah: Int, page: Int, note: String?) =
        repo.setAyahNote(surah, ayah, page, note)

    fun stopMark() = repo.stopMark()

    suspend fun setStop(surah: Int, ayah: Int, page: Int) = repo.setStop(surah, ayah, page)

    /**
     * يضع العلامةَ أو يرفعها — ويُرجع حالَها بعد الفعل.
     *
     * وهذا أوّلُ طريقٍ في التطبيق لإنشاء علامة: كانت شاشةُ العلامات
     * تعرض ولا شيءَ يكتب فيها، فتبقى فارغةً مهما فعل المستخدم.
     */
    /**
     * يحفظ موضعَ القراءة — «تابِع القراءة».
     *
     * جدولُ `QuranLastRead` وطرقُه (`getLastRead`/`saveLastRead`) كانت
     * موجودةً في القاعدة والمستودع منذ البداية و**بلا مستدعٍ واحد**:
     * لا شيءَ يكتب فيها ولا شيءَ يقرؤها، فالجدولُ فارغٌ أبداً.
     *
     * و`MushafPrefs.lastPage` في التفضيلات يفتح المصحفَ على آخر صفحةٍ
     * لكنّه لا يخرج من الشاشة: لا تراه الرئيسيةُ ولا يدخل في التصدير.
     * وهذا يفعل الاثنين.
     */
    /*  تسجيلُ الموضع يُسجّل وِردَ الختمة معه.
     *
     *  ولا يُطلب من القارئ أن يضغط «أتممتُ وِردي»: من قرأ فقد قرأ،
     *  والتطبيقُ يعرف أين بلغ. وزرُّ إقرارٍ زائدٌ يُنسى فيبدو صاحبُه
     *  متأخّراً وهو مواظب. */
    fun rememberPosition(surah: Int, ayah: Int, page: Int) {
        viewModelScope.launch {
            repo.saveLastRead(surah, ayah, page, 0f)
            //  الصفحةُ تُحتسب متى غادرها إلى ما بعدها — لا بمجرّد فتحها
            khatma.markRead(page - 1)
        }
    }

    fun lastRead(): Flow<LastReadPosition?> = repo.getLastRead()

    suspend fun toggleMark(surah: Int, ayah: Int, page: Int): Boolean =
        if (repo.isBookmarked(surah, ayah)) {
            repo.removeBookmarkByPosition(surah, ayah)
            false
        } else {
            repo.addBookmark(surah, ayah, page)
            true
        }
}
