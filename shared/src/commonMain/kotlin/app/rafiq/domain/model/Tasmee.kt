package app.rafiq.domain.model

/*
 * التسميع — المقابلةُ والحكم
 * ═════════════════════════
 *
 * ═══ القاعدةُ التي بُني عليها هذا الملفّ ═══
 *
 * **تغليطُ قارئٍ في كتاب الله بخطأِ نموذجٍ أسوأُ من غياب الميزة كلِّها.**
 *
 * فليس هذا حاكماً يقول «صحّ» أو «خطأ» ويسكت. له **ثلاثةُ** أحكام، والثالثُ
 * هو الذي يجعله حلالاً أن يُعرض على الناس: [Verdict.UNSURE] — «لم أتبيّن».
 * ومتى شكّ، سكت عن الاتّهام ولم يُسكت عن نفسه.
 *
 * ═══ وأين يقع الخطأُ فعلاً ═══
 *
 * النموذجُ يُخطئ في موضعين لا في موضع:
 *
 *   ١. **يُخطئ كلمةً** فيُسمِعها غيرَ ما قيل. وهذا يُحتمل: كلمةٌ من
 *      عشرين تُشار إليها، والقارئُ ينظر فيعرف.
 *   ٢. **يُخطئ التسجيلَ كلَّه** — صوتٌ بعيد، ضجيجٌ، لحنٌ بطيءٌ لم يتدرّب
 *      عليه. فيرجع بكلامٍ لا يشبه الآيةَ في شيء. وهنا **الخطأُ خطؤه لا
 *      خطأُ القارئ** — فلو قيل له «أخطأتَ في سبعَ عشرةَ كلمة» كان بهتاناً.
 *
 * فالفرقُ بين الحالتين هو كلُّ عمل هذا الملفّ: [SUSPECT_CEILING] يفصلهما.
 * قليلُ الخلل خللٌ في القراءة، وكثيرُه خللٌ في السمع.
 *
 * ═══ ولماذا الكلمةُ لا الحرف ═══
 *
 * التعرّفُ على الصوت يرجع بنصٍّ إملائيٍّ لا عثمانيّ، فالمقابلةُ الحرفيّةُ
 * تفشل في كل آية. والتطبيعُ هنا توأمُ تطبيع البحث في فكرته — حذفُ
 * التشكيل، وتوحيدُ الحروف، وحذفُ الألف — **إلّا أنّه يحفظ المسافات**،
 * وذاك تطبيعُ البحث يحذفها. ولا يُستعمل [ArabicSearch] هنا لذلك: من حذف
 * المسافاتَ لم يبقَ له كلماتٌ يقابلها، فلا يعرف **أيَّ** كلمةٍ أخطأ.
 *
 * ═══ والوصلُ والفصل ═══
 *
 * الرسمُ العثمانيُّ يصل ما يفصله الإملائيّ: «يَٰٓأَيُّهَا» كلمةٌ واحدةٌ فيه،
 * ومن سمعها كتبها «يا أيها» كلمتين. فالمحاذاةُ تعرف الوصلَ والفصل
 * ([ALIGN_MERGE] و[ALIGN_SPLIT])، وإلّا عُدَّت كلُّ آيةٍ فيها نداءٌ خطأً
 * في موضعين — وفي القرآن مئةٌ وأربعون نداءً.
 */

/** حالُ كلمةٍ من كلمات الآية بعد المقابلة. */
enum class WordMark {
    /** قُرئت كما هي. */
    HEARD,

    /** سُمع مكانَها غيرُها. */
    SUSPECT,

    /** لم تُسمع. */
    SILENT,
}

/**
 * حكمُ التسميع — ثلاثةٌ لا اثنان.
 */
enum class Verdict {
    /** تمّت التلاوةُ بلا خلل. */
    CLEAR,

    /** خللٌ في مواضعَ معلومةٍ تُشار إليها. */
    FLAWED,

    /**
     * **لم أتبيّن.** ولا يُعرَض معه اتّهامُ كلمةٍ واحدة.
     *
     * وهذا ليس فشلاً يُخفى: يُقال للقارئ صريحاً أنّ الآلةَ لم تسمع،
     * ويُطلب إليه أن يعيد. وهو أصدقُ من حكمٍ مبنيٍّ على سمعٍ رديء.
     */
    UNSURE,
}

/**
 * نتيجةُ تسميعِ آية.
 *
 * @param marks حالُ كلِّ كلمةٍ من كلمات الآية بترتيبها — وطولُها طولُ الآية
 *              دائماً. وفي [Verdict.UNSURE] كلُّها [WordMark.HEARD]: لا
 *              تُشار كلمةٌ بسوءٍ وقد قيل «لم أتبيّن».
 * @param heard كم كلمةً قُرئت كما هي
 * @param total كم كلمةً في الآية
 */
data class TasmeeResult(
    val verdict: Verdict,
    val marks: List<WordMark>,
    val heard: Int,
    val total: Int,
)

/**
 * أدنى ثقةٍ في النموذج يُبنى عليها حكم.
 *
 * ودونها لا يُقابَل النصُّ أصلاً: مقابلةُ كلامٍ لا يثق به قائلُه تُنتج
 * اتّهاماً لا معلومة.
 */
const val CONFIDENCE_FLOOR = 0.55f

/**
 * أقلُّ ما يُقبل من طول التلاوة نسبةً إلى الآية.
 *
 * من سمّع ثلاثَ كلماتٍ من آيةٍ في عشرين فالأغلبُ أنّه سكت أو انقطع
 * التسجيل — لا أنّه أخطأ سبعَ عشرة. فيُقال «لم أتبيّن» ويُطلب أن يعيد.
 */
const val LENGTH_FLOOR = 0.5f

/**
 * سقفُ الخلل الذي يبقى بعده الحكمُ على القارئ.
 *
 * فوقه يُرَدّ الخللُ إلى السمع لا إلى القراءة. وثلثُ الآية حدٌّ مقصود:
 * من أخطأ ثلثَ آيةٍ وهو يسمّعها لا يسمّع — ومن قال النموذجُ عنه ذلك فالغالبُ
 * أنّه لم يسمعه. والشكُّ هنا **في مصلحة القارئ**، فهذا كتابُ الله.
 */
const val SUSPECT_CEILING = 0.34f

/** يُقطّع النصَّ كلماتٍ مطبَّعةً، ويحفظ المسافاتِ فاصلاً. */
fun tasmeeTokens(text: String): List<String> =
    text.split(' ', '\n', '\t', ' ')
        .map(::tasmeeNormalize)
        .filter { it.isNotEmpty() }

/**
 * تطبيعُ كلمةٍ واحدة — بلا حذفِ المسافات.
 *
 * وقواعدُه قواعدُ [ArabicSearch.normalize] نفسُها في الحروف والتشكيل
 * والألف، ولا يُنادى ذاك لأنّه يحذف المسافاتِ فتضيع الكلمات.
 *
 * **والقواعدُ منسوخةٌ، والنسخُ يفترق.** فإن غُيّرت قواعدُ [ArabicSearch]
 * وجب أن تُغيَّر هنا معها — و`tools/check_tasmee_rules.py` يفشل البناءَ إن
 * افترقا، فلا يقع ذلك بصمت.
 */
fun tasmeeNormalize(word: String): String {
    var t = DIACRITICS.replace(word, "")
    for ((from, to) in LETTERS) t = t.replace(from, to)
    for ((from, to) in UTHMANI_WORDS) t = t.replace(from, to)
    return t.replace("ا", "").trim()
}

private val DIACRITICS = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")

private val LETTERS = mapOf(
    "ٱ" to "ا", "آ" to "ا", "أ" to "ا", "إ" to "ا",
    "ى" to "ي", "ة" to "ه", "ؤ" to "و", "ئ" to "ي", "ء" to "",
)

private val UTHMANI_WORDS = mapOf(
    "صلوه" to "صلاه", "زكوه" to "زكاه", "حيوه" to "حياه",
    "مشكوه" to "مشكاه", "منوه" to "مناه", "ربوا" to "ربا",
    "التورىه" to "التوراه",
)

/*  عملياتُ المحاذاة. */
private const val ALIGN_MATCH = 0
private const val ALIGN_SUSPECT = 1
private const val ALIGN_SILENT = 2
private const val ALIGN_EXTRA = 3

/** وصلٌ: كلمةٌ عثمانيّةٌ واحدةٌ سُمعت كلمتين. */
private const val ALIGN_MERGE = 4

/** فصلٌ: كلمتان عثمانيّتان سُمعتا كلمةً واحدة. */
private const val ALIGN_SPLIT = 5

/**
 * يُقابل ما سُمع بالآية ويحكم.
 *
 * @param ayah نصُّ الآية بالرسم العثمانيّ
 * @param heardText ما رجع به النموذجُ من كلام
 * @param confidence ثقةُ النموذج ٠..١ — وسالبُها أو ما دون
 *        [CONFIDENCE_FLOOR] يُسقط الحكمَ إلى [Verdict.UNSURE]
 */
fun tasmee(ayah: String, heardText: String, confidence: Float): TasmeeResult {
    val mushaf = tasmeeTokens(ayah)
    val heard = tasmeeTokens(heardText)

    //  آيةٌ بلا كلمات: لا حكمَ ولا اتّهام.
    if (mushaf.isEmpty()) return TasmeeResult(Verdict.UNSURE, emptyList(), 0, 0)

    val unsure = TasmeeResult(Verdict.UNSURE, List(mushaf.size) { WordMark.HEARD }, 0, mushaf.size)

    if (confidence < CONFIDENCE_FLOOR) return unsure
    if (heard.size < mushaf.size * LENGTH_FLOOR) return unsure

    val marks = align(mushaf, heard)
    val heardCount = marks.count { it == WordMark.HEARD }
    val flawed = mushaf.size - heardCount

    //  كثرةُ الخلل تُرَدّ إلى السمع لا إلى القارئ — وهي الحالةُ التي
    //  يصير فيها هذا الملفُّ نافعاً أو ضارّاً.
    if (flawed > mushaf.size * SUSPECT_CEILING) return unsure

    return TasmeeResult(
        verdict = if (flawed == 0) Verdict.CLEAR else Verdict.FLAWED,
        marks = marks,
        heard = heardCount,
        total = mushaf.size,
    )
}

/**
 * محاذاةُ قائمتَي كلماتٍ بأقلِّ خلل — ومعها الوصلُ والفصل.
 *
 * برمجةٌ ديناميكيّةٌ على جدولٍ `(m+1)×(h+1)`؛ وكلفةُ المطابقة والوصل
 * والفصل صفرٌ، وما سواها واحد. ثمّ يُتراجَع في الجدول فتُعلَم كلُّ كلمةٍ
 * من الآية بحالها.
 */
private fun align(mushaf: List<String>, heard: List<String>): List<WordMark> {
    val m = mushaf.size
    val h = heard.size
    val cost = Array(m + 1) { IntArray(h + 1) }
    val from = Array(m + 1) { IntArray(h + 1) }

    for (i in 1..m) {
        cost[i][0] = i
        from[i][0] = ALIGN_SILENT
    }
    for (j in 1..h) {
        cost[0][j] = j
        from[0][j] = ALIGN_EXTRA
    }

    for (i in 1..m) {
        for (j in 1..h) {
            var best = cost[i - 1][j] + 1
            var op = ALIGN_SILENT

            if (cost[i][j - 1] + 1 < best) {
                best = cost[i][j - 1] + 1
                op = ALIGN_EXTRA
            }

            val same = mushaf[i - 1] == heard[j - 1]
            val sub = cost[i - 1][j - 1] + if (same) 0 else 1
            if (sub < best) {
                best = sub
                op = if (same) ALIGN_MATCH else ALIGN_SUSPECT
            }

            //  وصلٌ: «يايها» سُمعت «يا» و«ايها»
            if (j >= 2 && mushaf[i - 1] == heard[j - 2] + heard[j - 1] && cost[i - 1][j - 2] < best) {
                best = cost[i - 1][j - 2]
                op = ALIGN_MERGE
            }

            //  فصلٌ: كلمتان في المصحف سُمعتا واحدة
            if (i >= 2 && mushaf[i - 2] + mushaf[i - 1] == heard[j - 1] && cost[i - 2][j - 1] < best) {
                best = cost[i - 2][j - 1]
                op = ALIGN_SPLIT
            }

            cost[i][j] = best
            from[i][j] = op
        }
    }

    val marks = MutableList(m) { WordMark.HEARD }
    var i = m
    var j = h
    while (i > 0) {
        when (from[i][j]) {
            ALIGN_MATCH -> { marks[i - 1] = WordMark.HEARD; i--; j-- }
            ALIGN_SUSPECT -> { marks[i - 1] = WordMark.SUSPECT; i--; j-- }
            ALIGN_SILENT -> { marks[i - 1] = WordMark.SILENT; i-- }
            ALIGN_EXTRA -> j--
            ALIGN_MERGE -> { marks[i - 1] = WordMark.HEARD; i--; j -= 2 }
            ALIGN_SPLIT -> {
                marks[i - 1] = WordMark.HEARD
                marks[i - 2] = WordMark.HEARD
                i -= 2
                j--
            }
            //  الزيادةُ وحدَها لا تُنقص من الآية شيئاً
            else -> { marks[i - 1] = WordMark.SILENT; i-- }
        }
    }
    return marks
}
