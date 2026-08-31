package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import org.json.JSONObject

/* ══════════════════════════════════════════════════════════════
   تخطيطُ المصحف — أين تقع كلُّ كلمةٍ من الورقة

   خطوطُ QCF لا تُصيّر حروفاً: كلُّ كلمةٍ قرآنيةٍ رمزٌ واحد في الخطّ،
   وموضعُه وعرضُه مضبوطان ليخرج السطرُ كما خرج في مصحف المدينة. فلا
   يكفي الخطُّ وحده — لا بدّ من جدولٍ يقول: هذه الصفحةُ خمسةَ عشرَ
   سطراً، وهذا السطرُ رموزُه كذا، وكلُّ رمزٍ لأيّ آية.

   وملفُّ المصدر ٦٠٤ ملفَّ JSON مجموعُها ٢٠ ميغابايت، وأكثرُها تكرارُ
   أسماءِ الحقول. فأُعيدت صياغتُه: أساسٌ واحدٌ للصفحة وفروقٌ صغيرة،
   فصار ١٫١ ميغابايت — وفي الحزمة المضغوطة ثمانين كيلوبايت.

   وتحقّقٌ من سلامته: ٦٢٣٦ علامةَ نهاية آية (وهو عددُ آيات المصحف)،
   و١١٤ لوحَ سورة، و١١٢ بسملة — والفاتحةُ بسملتُها آيةٌ فيها وبراءةُ
   بلا بسملة، فيصحّ العدد.
══════════════════════════════════════════════════════════════ */

/** نوعُ العنصر في السطر. */
object GlyphType {
    const val WORD = 0
    const val AYAH_END = 1
    const val SURAH_HEADER = 2
    const val BISMILLAH = 3
    const val QUARTER = 4
}

data class MushafPage(
    /** رقمُ الصفحة ١–٦٠٤. */
    val p: Int,
    /** أصغرُ رمزٍ في الصفحة — والرموزُ تُخزَّن فروقاً عنه. */
    val b: Int,
    /** الأسطر، وكلُّ سطرٍ فروقُ رموزه. */
    val l: List<List<Int>>,
    /** مفتاحُ الآية لكلِّ عنصرٍ بالترتيب — «٢:٦». */
    val v: List<String>,
    /** نوعُ كلِّ عنصر — راجع [GlyphType]. */
    val t: List<Int>,
    /** أرقامُ السور لألواح العناوين في هذه الصفحة. */
    val s: List<Int> = emptyList(),
) {
    /** الرمزُ الحقيقيّ كمحرفٍ من منطقة الاستعمال الخاصّ. */
    fun glyph(delta: Int): String = String(Character.toChars(b + delta))
}

data class MushafLayout(
    val v: Int,
    val fonts: List<String>,
    /** خطُّ كلِّ صفحة — فهرسٌ في [fonts]، ٦٠٤ عنصراً. */
    val pageFont: List<Int>,
    val pages: List<MushafPage>,
) {
    private val byPage by lazy { pages.associateBy { it.p } }

    fun page(n: Int): MushafPage? = byPage[n]

    /** اسمُ ملفّ الخطّ لصفحةٍ ما — «QCF4_Hafs_01». */
    fun fontOf(page: Int): String? =
        pageFont.getOrNull(page - 1)?.let { fonts.getOrNull(it) }

    companion object {
        private const val ASSET = "mushaf_layout.json"

        @Volatile private var cached: MushafLayout? = null

        /*  يُقرأ بـ`org.json` لا بمكتبةٍ خارجية: هو في أندرويد نفسِه، ولا
            حزمةَ جديدةً تُضاف من أجل ملفٍّ واحد. */
        fun load(context: Context): MushafLayout =
            cached ?: synchronized(this) {
                cached ?: parse(
                    context.assets.open(ASSET).bufferedReader().use { it.readText() },
                ).also { cached = it }
            }

        private fun parse(raw: String): MushafLayout {
            val root = JSONObject(raw)
            val fontsArr = root.getJSONArray("fonts")
            val fonts = List(fontsArr.length()) { fontsArr.getString(it) }
            val pfArr = root.getJSONArray("pageFont")
            val pageFont = List(pfArr.length()) { pfArr.getInt(it) }
            val pagesArr = root.getJSONArray("pages")
            val pages = ArrayList<MushafPage>(pagesArr.length())
            for (i in 0 until pagesArr.length()) {
                val o = pagesArr.getJSONObject(i)
                val lArr = o.getJSONArray("l")
                val lines = List(lArr.length()) { r ->
                    val row = lArr.getJSONArray(r)
                    List(row.length()) { row.getInt(it) }
                }
                val vArr = o.getJSONArray("v")
                val tArr = o.getJSONArray("t")
                val sArr = o.optJSONArray("s")
                pages += MushafPage(
                    p = o.getInt("p"),
                    b = o.getInt("b"),
                    l = lines,
                    v = List(vArr.length()) { vArr.getString(it) },
                    t = List(tArr.length()) { tArr.getInt(it) },
                    s = if (sArr == null) emptyList() else List(sArr.length()) { sArr.getInt(it) },
                )
            }
            return MushafLayout(root.optInt("v", 1), fonts, pageFont, pages)
        }
    }
}
