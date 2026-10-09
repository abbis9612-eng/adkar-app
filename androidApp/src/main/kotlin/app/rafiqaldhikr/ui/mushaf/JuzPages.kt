package app.rafiqaldhikr.ui.mushaf

/**
 * أوّلُ صفحةٍ في كلِّ جزءٍ — ثلاثون رقماً.
 *
 * ولِمَ جدولٌ في الكود والجزءُ مخزونٌ في `mushaf_layout.json`؟ لأنّ ذاك
 * الملفَّ **ميغابايتٌ وربع**، ولا يُحمَّل من أجل كلمةِ «الجزء ١٢» في
 * بطاقةِ ختمة. وثلاثون رقماً لا تحتاج ملفّاً.
 *
 * **ولا تُكتب بالتقدير:** `check_quran_data.py` يعيد حسابها من
 * `quran_uthmani.json` ويقابلها بهذا الجدول، فيُسقط البناءَ إن افترقا.
 * فالجدولُ نسخةٌ سريعةٌ من البيانات لا رأيٌ فيها.
 */
internal val JUZ_FIRST_PAGE = intArrayOf(
    1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
    201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
    402, 422, 442, 462, 482, 502, 522, 542, 562, 582,
)

/** جزءُ صفحةٍ ما ١–٣٠ — وصفحةٌ خارجَ المدى تُقصَر على طرفه. */
fun juzOfPage(page: Int): Int {
    val p = page.coerceIn(1, 604)
    var lo = 0
    var hi = JUZ_FIRST_PAGE.size - 1
    var found = 0
    while (lo <= hi) {
        val mid = (lo + hi) / 2
        if (JUZ_FIRST_PAGE[mid] <= p) { found = mid; lo = mid + 1 } else hi = mid - 1
    }
    return found + 1
}
