package app.rafiq.domain.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/*
 * وِردُ اليوم من الختمة
 * ════════════════════
 *
 * ═══ ثلاثةُ قراراتٍ تجعلها أفضلَ من «صفحاتٌ في اليوم» ═══
 *
 * **١ · الوِردُ يتكيّف ولا يتراكم صامتاً.**
 *   الختمةُ الساذجةُ تقسم ٦٠٤ على ٣٠ فتقول «٢٠ صفحة» كلَّ يوم. فمن فاته
 *   ثلاثةُ أيّامٍ صار عليه ٨٠ صفحةً في يومٍ واحد، فييأس ويترك الختمةَ
 *   كلَّها. وهنا يُعاد القسمُ على **ما بقي من الأيّام**، ويُقال له صراحةً
 *   كم تأخّر — فيختار: يُلحِق أو يمدّ.
 *
 * **٢ · يُقاس بالوقت لا بالصفحات وحدَها.**
 *   «٢٠ صفحة» لا تقول شيئاً لمن لا يعرف كم تأخذ. و«نحو ٣٥ دقيقة» تقول.
 *   والتقديرُ [SECONDS_PER_PAGE] **تقديرٌ لا قياس**، ويُعرض على أنّه كذلك.
 *
 * **٣ · لا يُلام من سبق.**
 *   من قرأ أكثرَ من وِرده لا يُعاد عليه ما قرأ: الوِردُ يبدأ دائماً من
 *   بعد آخر صفحةٍ قرأها فعلاً، لا من موضعه المتوقَّع في الجدول.
 *
 * ═══ وما لم يُفعل، ويُقال ═══
 *
 * الوِردُ يُقطَع **عند حدّ صفحة** لا عند حدّ آية. وصفحةُ المصحف قد تنتهي
 * في وسط آية، فالقطعُ عند حدّ الآية كان أجمل. لكنّه يجعل كلَّ يومٍ يبدأ
 * في وسط صفحةٍ في مصحفٍ مبنيٍّ على الصفحات، فيضيع أثرُ «افتح على صفحة
 * ٤٥» — وذاك أسوأ. فالصفحةُ هي الوحدة، وهي وحدةُ الوِرد عند الناس.
 */

/** تقديرُ زمن الصفحة الواحدة بالثواني — **تقديرٌ لا قياس**. */
const val SECONDS_PER_PAGE = 100

/**
 * ما على القارئ اليوم.
 *
 * @param from أوّلُ صفحةٍ · [to] آخرُها (داخلتان)
 * @param pages عددُها · [minutes] تقديرُ زمنها
 * @param behind كم صفحةً تأخّر عن الجدول — صفرٌ إن كان في موعده أو سابقاً
 * @param daysLeft كم يوماً بقي من الختمة (اليومُ منها)
 * @param done هل أتمّ وِردَ اليوم
 * @param finished هل بلغ آخرَ الختمة
 */
data class Wird(
    val from: Int,
    val to: Int,
    val pages: Int,
    val minutes: Int,
    val behind: Int,
    val daysLeft: Int,
    val done: Boolean,
    val finished: Boolean,
)

/**
 * يحسب وِردَ اليوم.
 *
 * @param fromPage أوّلُ صفحةٍ في الختمة · [toPage] آخرُها
 * @param days مدّةُ الختمة بالأيّام
 * @param startedOn يومُ البدء منذ المبدأ
 * @param readTo آخرُ صفحةٍ قُرئت فعلاً (صفرٌ = لم يبدأ)
 * @param today اليومُ الحاضر منذ المبدأ
 */
fun wirdOf(
    fromPage: Int,
    toPage: Int,
    days: Int,
    startedOn: Long,
    readTo: Int,
    today: Long,
): Wird {
    val total = (toPage - fromPage + 1).coerceAtLeast(1)
    //  موضعُ القارئ: ما لم يبدأ فهو قبل الصفحة الأولى
    val cursor = if (readTo < fromPage) fromPage - 1 else min(readTo, toPage)
    val remaining = toPage - cursor

    if (remaining <= 0) {
        return Wird(
            from = toPage, to = toPage, pages = 0, minutes = 0,
            behind = 0, daysLeft = 0, done = true, finished = true,
        )
    }

    /*  اليومُ الأوّلُ من الختمة هو اليومُ صفر. والسالبُ (ختمةٌ تبدأ غداً)
     *  يُعامَل صفراً: لا يُطالَب أحدٌ بوِردٍ قبل أن تبدأ ختمتُه. */
    val dayIndex = max(0L, today - startedOn).toInt()
    //  يومٌ واحدٌ على الأقلّ: القسمةُ على صفرٍ تُسقط الحساب، ومن تجاوز
    //  مدّتَه يُطالَب بالباقي كلِّه في يومه لا بلا شيء.
    val daysLeft = max(1, days - dayIndex)

    /*  القسمُ على **ما بقي** لا على المدّة كلِّها.
     *
     *  فمن فاته ثلاثةُ أيّامٍ في ختمةِ ثلاثين لا يُطالَب بثمانين صفحةً
     *  دفعةً، بل يُعاد توزيعُ الباقي على ما بقي من أيّامه. */
    val perDay = ceilDiv(remaining, daysLeft)

    val start = cursor + 1
    val end = min(toPage, cursor + perDay)
    val pages = end - start + 1

    /*  التأخّرُ: الفرقُ بين ما كان ينبغي أن يُقرأ وما قُرئ.
     *
     *  ويُحسب بالجدول الأصليّ لا بالمعدَّل — وإلّا لما ظهر تأخّرٌ أبداً،
     *  إذ يُعاد القسمُ كلَّ يومٍ فيبدو صاحبُه في موعده دائماً وهو متأخّر. */
    val expected = min(total, ceilDiv(total * dayIndex, max(1, days)))
    val actual = cursor - (fromPage - 1)
    val behind = max(0, expected - actual)

    return Wird(
        from = start,
        to = end,
        pages = pages,
        minutes = ((pages * SECONDS_PER_PAGE) / 60.0).roundToInt().coerceAtLeast(1),
        behind = behind,
        daysLeft = daysLeft,
        //  أتمّ وِردَ اليوم: قرأ بقدر ما كان عليه اليوم أو أكثر
        done = actual >= min(total, ceilDiv(total * (dayIndex + 1), max(1, days))),
        finished = false,
    )
}

/** قسمةٌ لأعلى — بلا عوّامات، فلا خطأَ تقريبٍ يُضيع صفحة. */
private fun ceilDiv(a: Int, b: Int): Int = if (b <= 0) a else (a + b - 1) / b
