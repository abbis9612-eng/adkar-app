package app.rafiq.domain.model

/**
 * مدينة يختارها المستخدم يدوياً حين لا يمنح إذن الموقع.
 *
 * مكانها في shared لا في طبقة الأندرويد: هي بيانات، وتحليل JSON يحتاج
 * kotlinx.serialization وهي معرَّفة هنا وحدها. وضعُها في androidApp كان
 * سيتطلّب إضافة المكتبة ومُلحِقها إلى وحدة ثانية بلا داعٍ.
 */
data class City(
    val ar:        String,
    val en:        String,
    val country:   String,
    val countryAr: String,
    val lat:       Double,
    val lng:       Double,
) {
    /**
     * مطابقة البحث. تسامح ما يكتبه الناس فعلاً: بلا تشكيل، وهمزة وصل بدل
     * قطع، و«ه» بدل «ة»، وبأل التعريف أو بدونها — فمن يكتب «الاسكندرية»
     * يقصد «الإسكندرية»، ومن يكتب «مكه» يقصد «مكة».
     */
    fun matches(query: String): Boolean {
        val q = normalize(query)
        return q.isNotEmpty() &&
            (q in normalize(ar) || q in normalize(en) || q in normalize(countryAr))
    }

    private fun normalize(s: String): String = buildString {
        for (ch in s.lowercase()) {
            when {
                ch in 'ً'..'ْ' || ch == 'ٰ' -> {}   // التشكيل
                ch.isWhitespace()                          -> {}
                ch == 'أ' || ch == 'إ' || ch == 'آ'        -> append('ا')
                ch == 'ى'                                  -> append('ي')
                ch == 'ة'                                  -> append('ه')
                else                                       -> append(ch)
            }
        }
    }.replace("ال", "")   // أل التعريف — بعد التطبيع حتى تُطابَق «الإسكندرية»
}
