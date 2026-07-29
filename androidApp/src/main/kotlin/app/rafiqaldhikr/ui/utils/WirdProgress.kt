package app.rafiqaldhikr.ui.utils

import app.rafiq.domain.model.DailyProgressInfo

/**
 * درجة الورد اليومي على سلّم ٠-١٠٠٠ — مصدر حقيقة واحد يشترك فيه HomeViewModel
 * وشاشة مُداوَمَتي، بدل تكرار الصيغة في أكثر من ملف.
 */
fun calculateWirdProgress(progress: DailyProgressInfo?): Int {
    if (progress == null) return 0
    var score = 0
    if (progress.morningDone) score += 200
    if (progress.eveningDone) score += 200
    score += (progress.quranPages * 50).toInt().coerceAtMost(300)
    score += progress.tasbeehCount.toInt().coerceAtMost(200)
    score += (progress.prayersLogged * 20).toInt().coerceAtMost(100)
    return score.coerceAtMost(1000)
}

/** الحدّ الأدنى على سلّم الورد — دورة تسبيحٍ واحدة (٣٣) لا تُقاس بالكثرة بل بالمداومة. */
const val WIRD_MINIMUM = 33
