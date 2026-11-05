package app.rafiq.domain.repository

/**
 * إدارة بيانات المستخدم: التصدير الكامل كـ JSON والاستيراد والحذف النهائي.
 * لا يمس المحتوى الشرعي المضمّن (القرآن، الأذكار، الأدعية) — فقط بيانات الاستخدام.
 */
interface UserDataRepository {
    suspend fun exportAsJson(): String

    /**
     * يستعيد ملفَّ تصديرٍ سابق.
     *
     * كان التصديرُ يعمل ولا استيرادَ معه: يُخرج المستخدم ملفَّه، ثمّ يبدّل
     * جهازَه أو يمسح بيانات التطبيق، فلا سبيلَ لإعادة ما صدَّر. زرُّ
     * نسخٍ احتياطيٍّ بلا استعادةٍ ليس نسخاً احتياطياً.
     *
     * ولا يُلمَس شيءٌ قبل التحقّق من الصيغة: يُقرأ الملفُّ كلُّه ويُتحقّق
     * من `formatVersion` أوّلاً، ثمّ تُكتب البيانات في معاملةٍ واحدة —
     * فإمّا استيرادٌ تامّ وإمّا لا شيء، ولا حالَ بينهما.
     */
    suspend fun importFromJson(json: String): ImportResult

    /** يمسح بياناتِ الاستعمال كلَّها — ولا يمسّ المحتوى الشرعيّ المضمَّن. */
    suspend fun clearAllUserData()
}

/** نتيجةُ الاستيراد — تُعرض للمستخدم كما هي، لا «تمّ» مجرّدة. */
sealed interface ImportResult {
    data class Success(
        val days:      Int,
        val sessions:  Int,
        val prayers:   Int,
        val bookmarks: Int,
        val adhkar:    Int,
    ) : ImportResult

    /** الملفُّ ليس تصديراً لهذا التطبيق، أو صيغتُه أحدثُ ممّا نعرف. */
    data class Invalid(val reason: Reason) : ImportResult

    enum class Reason { NOT_JSON, NOT_RAFIQ, FUTURE_VERSION }
}
