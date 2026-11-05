package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import org.json.JSONArray

/** أسماءُ السور من `surah_metadata.json` — تُقرأ مرّةً وتُحفظ. */
object SurahNames {
    @Volatile private var names: List<String>? = null
    @Volatile private var starts: List<Int>? = null

    fun of(context: Context, surah: Int): String {
        ensure(context)
        return names?.getOrNull(surah - 1).orEmpty()
    }

    /**
     * اسمُ سورةِ صفحةٍ ما — آخرُ سورةٍ بدأت عندها أو قبلها.
     *
     * ولا يُحمَّل لها `mushaf_layout.json` (١٫٢ ميغا) من أجل اسم: هذا
     * الأصلُ ثمانيةٌ وعشرون كيلو، وفيه `page_start` لكلِّ سورة — فبحثٌ
     * ثنائيٌّ في مئةٍ وأربعةَ عشرَ رقماً يكفي.
     *
     * و`page_start` هو صفحةُ **بداية** السورة لا نهايتُها: سورةُ هود
     * تبدأ في أسفل الصفحة ٢٢١، فصفحةُ ٢٢٢ هودٌ وإن لم تبدأ فيها.
     */
    fun atPage(context: Context, page: Int): String {
        ensure(context)
        val st = starts ?: return ""
        if (st.isEmpty()) return ""
        var lo = 0
        var hi = st.size - 1
        var found = 0
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (st[mid] <= page) { found = mid; lo = mid + 1 } else hi = mid - 1
        }
        return names?.getOrNull(found).orEmpty()
    }

    private fun ensure(context: Context) {
        if (names != null) return
        synchronized(this) {
            if (names != null) return
            val (n, s) = load(context)
            starts = s
            names = n
        }
    }

    private fun load(context: Context): Pair<List<String>, List<Int>> = runCatching {
        val arr = JSONArray(
            context.assets.open("surah_metadata.json").bufferedReader().use { it.readText() },
        )
        val n = ArrayList<String>(arr.length())
        val s = ArrayList<Int>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            n += o.optString("name_ar")
            s += o.optInt("page_start", 1)
        }
        n to s
    }.getOrDefault(emptyList<String>() to emptyList())
}
