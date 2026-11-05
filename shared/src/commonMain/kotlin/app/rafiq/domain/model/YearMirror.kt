package app.rafiq.domain.model

/*
 * «سنتك» — المرآة
 * ═══════════════
 *
 * ═══ لماذا ليست «إحصائيّات» ═══
 *
 * الإحصائيّاتُ تقول «٤٧ يوماً · ٣١٢ صفحة · ٨٩٠٠ تسبيحة». وهي أرقامٌ
 * صادقةٌ لا تُحرّك أحداً: من رآها عرف كم فعل، ولم يَرَ **كيف كان عامُه**.
 *
 * والمرآةُ تُري الشكلَ لا العدد: ثلاثمئةٍ وخمسةٌ وستّون مربّعاً، كلُّ
 * مربّعٍ يومٌ ولونُه عمقُ ما كان فيه. فيُرى الانقطاعُ خطّاً باهتاً،
 * وتُرى المواظبةُ رقعةً داكنة، ويُرى رمضانُ من بعيد.
 *
 * ═══ والعمقُ ليس مجموعاً ═══
 *
 * لو جُمعت الأرقامُ لطغى التسبيح: من سبّح ألفاً في يومٍ وترك كلَّ شيءٍ
 * غيرَه بدا يومُه أعمقَ من يومٍ صلّى فيه وقرأ وذكر. فالعمقُ **عددُ
 * الأبواب المطروقة** لا مقدارُ الطرق: خمسةُ أبوابٍ متساوية، ومن طرق
 * أربعةً أعمقُ ممّن أكثر في واحد.
 */

/** يومٌ في المرآة: تاريخُه وعمقُه ٠..٥. */
data class MirrorDay(val date: String, val depth: Int)

/** خلاصةُ العام — بالعربية لا بالأرقام الجافّة. */
data class MirrorSummary(
    /** أيّامٌ فيها عملٌ ما. */
    val activeDays: Int,
    /** أطولُ سلسلةٍ متّصلة. */
    val longestStreak: Int,
    val quranPages: Long,
    val tasbeeh: Long,
    val prayers: Long,
    /** البابُ الذي واظب عليه أكثر — أو null إن لم يكن عملٌ. */
    val mostKept: Door?,
)

/** الأبوابُ الخمسةُ التي يُقاس بها عمقُ اليوم — متساويةٌ في الوزن. */
enum class Door { MORNING, EVENING, QURAN, TASBEEH, PRAYERS }

/**
 * عمقُ يومٍ — **عددُ الأبواب المطروقة** لا مجموعُ الأرقام.
 *
 * ولو جُمعت الأرقامُ لطغى التسبيح: من سبّح ألفاً وترك ما سواه بدا يومُه
 * أعمقَ من يومٍ صلّى فيه صاحبُه وقرأ وذكر. والخمسةُ متساوية.
 */
fun depthOf(d: DailyProgressInfo): Int {
    var n = 0
    if (d.morningDone) n++
    if (d.eveningDone) n++
    if (d.quranPages > 0) n++
    if (d.tasbeehCount > 0) n++
    if (d.prayersLogged > 0) n++
    return n
}

/**
 * يبني مرآةَ السنة من سجلّ الأيّام.
 *
 * @param days الأيّامُ المسجَّلة — ناقصةً، فالأيّامُ الخاليةُ لا صفَّ لها
 * @param allDates تواريخُ العام كلِّها بالترتيب
 */
fun mirrorOf(days: List<DailyProgressInfo>, allDates: List<String>): List<MirrorDay> {
    val byDate = days.associateBy { it.date }
    //  اليومُ الذي لا صفَّ له عمقُه صفر — لا يُحذف من الشبكة.
    //  وحذفُه كان يُزيح ما بعده فتكذب الشبكةُ عن مواضع الأيّام.
    return allDates.map { MirrorDay(it, byDate[it]?.let(::depthOf) ?: 0) }
}

/**
 * أطولُ سلسلةٍ متّصلةٍ من أيّامٍ فيها عملٌ ما.
 *
 * وتُحسب من الشبكة لا من `StreakData`: ذاك يحمل السلسلةَ الجاريةَ
 * وأطولَ ما بلغته **منذ بدء الاستعمال**، وهذه خلاصةُ **عامٍ بعينه**.
 */
fun longestRun(mirror: List<MirrorDay>): Int {
    var best = 0
    var run = 0
    for (d in mirror) {
        if (d.depth > 0) { run++; if (run > best) best = run } else run = 0
    }
    return best
}

/** خلاصةُ العام. */
fun summarize(days: List<DailyProgressInfo>, mirror: List<MirrorDay>): MirrorSummary {
    val counts = mapOf(
        Door.MORNING to days.count { it.morningDone }.toLong(),
        Door.EVENING to days.count { it.eveningDone }.toLong(),
        Door.QURAN to days.count { it.quranPages > 0 }.toLong(),
        Door.TASBEEH to days.count { it.tasbeehCount > 0 }.toLong(),
        Door.PRAYERS to days.count { it.prayersLogged > 0 }.toLong(),
    )
    return MirrorSummary(
        activeDays = mirror.count { it.depth > 0 },
        longestStreak = longestRun(mirror),
        quranPages = days.sumOf { it.quranPages },
        tasbeeh = days.sumOf { it.tasbeehCount },
        prayers = days.sumOf { it.prayersLogged },
        //  ولا يُقال «أكثرُ ما واظبتَ عليه» لمن لم يواظب على شيء.
        mostKept = counts.filterValues { it > 0 }.maxByOrNull { it.value }?.key,
    )
}
