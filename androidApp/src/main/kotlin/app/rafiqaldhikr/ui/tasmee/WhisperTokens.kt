package app.rafiqaldhikr.ui.tasmee

/*
 * معجمُ whisper — رموزٌ ← عربيّة
 * ═════════════════════════════
 *
 * النموذجُ يُخرج أرقاماً. وهذا يحوّلها نصّاً.
 *
 * ═══ ولماذا ليست جدولَ «رقمٌ ← حرف» ═══
 *
 * ترميزُ whisper **على مستوى البايت**: الرمزُ لا يحمل حرفاً بل بايتاتٍ
 * من UTF-8، والحرفُ العربيُّ بايتان فيأتي موزَّعاً على رمزين أو ثلاثة.
 * فمن جمع نصوصَ الرموز كما هي خرج بنصٍّ مشوَّهٍ لا يُقرأ.
 *
 * وبايتاتُ UTF-8 لا تُكتب في ملفّ معجمٍ كما هي — فيها ما لا يُطبَع.
 * فيُرمَز كلُّ بايتٍ بمحرفٍ مرئيّ: البايتُ ٢٢٤ يُكتب `à`، والسطرُ
 * الجديدُ يُكتب `Ċ`. وهذا الملفُّ يعكس ذلك الترميزَ ثمّ يفكّ UTF-8.
 *
 * ═══ والرموزُ الخاصّةُ تُسقَط ═══
 *
 * `<|ar|>` و`<|transcribe|>` و`<|notimestamps|>` ليست كلاماً يُعرض. وهي
 * في `tokens.txt` **سطورٌ فارغة** — قرارٌ من المولّد لا نقصٌ فيه. فتُسقَط
 * هنا بشرطٍ واحدٍ بلا قائمةِ استثناءاتٍ تُصان وتُنسى.
 */

/**
 * معجمُ النموذج مقروءاً من `assets/tasmee/tokens.txt`.
 *
 * @param pieces نصُّ كلِّ رمزٍ بترتيب رقمه — والفارغُ رمزٌ خاصٌّ يُسقَط
 */
class WhisperTokens(private val pieces: List<String>) {

    /** كم رمزاً يعرف. */
    val size: Int get() = pieces.size

    /**
     * يفكّ سلسلةَ أرقامٍ إلى نصٍّ عربيّ.
     *
     * والمحرفُ المجهولُ يُسقَط ولا يُستبدل بعلامةِ استفهام: نصٌّ فيه
     * «؟» يُعرَض على القارئ كأنّه ما قرأه.
     */
    fun decode(ids: List<Int>): String {
        val bytes = ArrayList<Byte>(ids.size * 3)
        for (id in ids) {
            val piece = pieces.getOrNull(id) ?: continue
            if (piece.isEmpty()) continue
            for (ch in piece) {
                val b = BYTE_OF_CHAR[ch] ?: continue
                bytes.add(b.toByte())
            }
        }
        return String(bytes.toByteArray(), Charsets.UTF_8)
    }

    companion object {
        /** يقرأ المعجمَ من نصّ الملفّ — سطرٌ لكلّ رمزٍ بترتيب رقمه. */
        fun parse(text: String): WhisperTokens =
            WhisperTokens(text.split('\n').let {
                //  آخرُ سطرٍ فارغٌ من محرف السطر الختاميّ — لا رمزٌ
                if (it.isNotEmpty() && it.last().isEmpty()) it.dropLast(1) else it
            })

        /**
         * محرفٌ ← بايت — عكسُ ترميز GPT-2 الذي يستعمله whisper.
         *
         * البايتاتُ المطبوعةُ تُمثّل نفسَها، وما سواها يُنقل إلى مدًى
         * مرئيٍّ يبدأ عند ٢٥٦. والترتيبُ هنا هو الترتيبُ هناك بالضبط —
         * فتبديلُ مداه يفكّ النصَّ عربيّةً مشوّهة.
         */
        private val BYTE_OF_CHAR: Map<Char, Int> = buildMap {
            val printable = ArrayList<Int>()
            for (b in 33..126) printable.add(b)
            for (b in 161..172) printable.add(b)
            for (b in 174..255) printable.add(b)
            val chars = ArrayList(printable)
            var n = 0
            for (b in 0..255) {
                if (b !in printable) {
                    printable.add(b)
                    chars.add(256 + n)
                    n++
                }
            }
            for (i in printable.indices) put(chars[i].toChar(), printable[i])
        }
    }
}
