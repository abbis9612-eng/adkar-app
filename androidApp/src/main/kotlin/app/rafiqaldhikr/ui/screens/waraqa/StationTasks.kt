package app.rafiqaldhikr.ui.screens.waraqa

import android.content.Context
import androidx.annotation.StringRes
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import org.json.JSONArray

/**
 * مهامُّ المحطّة — ما يُفعل فيها، لا اسمُها.
 *
 * كانت بطاقةُ المحطّة تقول «العصر وأذكار المساء» وتسكت، وفي وسطها فراغ.
 * وهذه تفصّلها: «صلاة العصر» و«أذكار المساء · ٢٣» — لكلٍّ علامتُه.
 *
 * ═══ أين تُحفظ كلُّ علامة ═══
 *
 *  - [Kind.PRAYER] في `PrayerLog` — **الجدولُ نفسُه الذي تحيط الورقةُ
 *    صلواتِه بالقلم**. فالصلاةُ تُعلَّم في مكانٍ فتتعلّم في الآخر، ولا
 *    تُسجَّل مرّتين.
 *  - [Kind.ADHKAR] و[Kind.DEED] في `DayStationLog` بمفتاحٍ مثل
 *    `isha.witr`. والجدولُ يقبل أيَّ نصٍّ مفتاحاً، فلا جدولَ جديدَ ولا
 *    ترحيل. وأذكارُ الصباح والمساء تُعدّ تامّةً أيضاً إن سجّلها
 *    `DailyProgress` من شاشة القراءة.
 */
data class StationTask(
    /** مفتاحُ `DayStationLog`، أو اسمُ الصلاة لـ[Kind.PRAYER]. */
    val key: String,
    @StringRes val label: Int,
    val kind: Kind,
    /** بابُ الأذكار في `adhkar_<key>.json` — للعدد ولفتح القراءة. */
    val adhkar: String? = null,
    /** وجهةٌ خاصّة إن لم تكن بابَ أذكار — كصفحةِ الكهف في المصحف. */
    val route: String? = null,
) {
    enum class Kind { PRAYER, ADHKAR, DEED }

    val opens: String? get() = route ?: adhkar?.let { RafiqRoute.DhikrReading.withCategory(it) }
}

object StationTasks {

    /** أوّلُ صفحاتِ الكهف في المصحف المدنيّ — كما في `DayCompanionViewModel`. */
    private const val KAHF_PAGE = 293

    fun of(stationId: String, wirdRoute: String? = null): List<StationTask> = when (stationId) {
        "wake" -> listOf(
            StationTask("wake.dua", R.string.task_wake_dua, StationTask.Kind.DEED),
            StationTask("wake.siwak", R.string.task_siwak, StationTask.Kind.DEED),
        )
        "fajr_morning" -> listOf(
            StationTask("fajr", R.string.task_fajr, StationTask.Kind.PRAYER),
            StationTask("fajr_morning.adhkar", R.string.task_morning, StationTask.Kind.ADHKAR, adhkar = "morning"),
        )
        "duha" -> listOf(
            StationTask("duha.pray", R.string.task_duha, StationTask.Kind.DEED),
        )
        "dhuhr" -> listOf(
            StationTask("dhuhr", R.string.task_dhuhr, StationTask.Kind.PRAYER),
            StationTask("dhuhr.adhkar", R.string.task_after_prayer, StationTask.Kind.ADHKAR, adhkar = "prayer"),
        )
        "asr_evening" -> listOf(
            StationTask("asr", R.string.task_asr, StationTask.Kind.PRAYER),
            StationTask("asr_evening.adhkar", R.string.task_evening, StationTask.Kind.ADHKAR, adhkar = "evening"),
        )
        "maghrib" -> listOf(
            StationTask("maghrib", R.string.task_maghrib, StationTask.Kind.PRAYER),
            StationTask("maghrib.adhkar", R.string.task_after_prayer, StationTask.Kind.ADHKAR, adhkar = "prayer"),
        )
        "isha" -> listOf(
            StationTask("isha", R.string.task_isha, StationTask.Kind.PRAYER),
            StationTask("isha.witr", R.string.task_witr, StationTask.Kind.DEED),
        )
        "sleep" -> listOf(
            StationTask("sleep.adhkar", R.string.task_sleep, StationTask.Kind.ADHKAR, adhkar = "sleep"),
        )
        "friday_kahf" -> listOf(
            StationTask("friday_kahf.read", R.string.task_kahf, StationTask.Kind.DEED,
                route = RafiqRoute.Mushaf.atPage(KAHF_PAGE)),
            StationTask("friday_kahf.salawat", R.string.task_salawat, StationTask.Kind.DEED),
        )
        "wird" -> listOf(
            StationTask("wird.read", R.string.task_wird, StationTask.Kind.DEED, route = wirdRoute),
        )
        else -> emptyList()
    }

    /**
     * هل تمّت المهمّة؟
     *
     * @param prayed     الصلواتُ المسجَّلةُ اليوم في `PrayerLog`.
     * @param completed  مفاتيحُ `DayStationLog` لليوم.
     * @param morning    أذكارُ الصباح مسجَّلةٌ في `DailyProgress`.
     * @param evening    أذكارُ المساء مسجَّلةٌ في `DailyProgress`.
     */
    fun isDone(
        task: StationTask,
        prayed: Set<String>,
        completed: Set<String>,
        morning: Boolean,
        evening: Boolean,
    ): Boolean = when (task.kind) {
        StationTask.Kind.PRAYER -> task.key in prayed
        else -> task.key in completed ||
            (task.adhkar == "morning" && morning) ||
            (task.adhkar == "evening" && evening)
    }

    /** المحطّةُ تامّةٌ إذا تمّت مهامُّها كلُّها — أو سُجّلت هي نفسُها تامّة. */
    fun isStationDone(
        stationId: String,
        tasks: List<StationTask>,
        prayed: Set<String>,
        completed: Set<String>,
        morning: Boolean,
        evening: Boolean,
    ): Boolean = stationId in completed ||
        (tasks.isNotEmpty() && tasks.all { isDone(it, prayed, completed, morning, evening) })
}

/**
 * عددُ أذكار الباب — «أذكار المساء · ٢٣».
 *
 * يُقرأ من أصل التطبيق (`adhkar_<key>.json`) لا يُكتب في الكود: من زاد
 * في الملفّ ذكراً لم يحتج أن يتذكّر هذا الرقم.
 */
object AdhkarCount {
    private val cache = HashMap<String, Int>(8)

    fun of(context: Context, key: String): Int? = synchronized(cache) {
        cache[key] ?: runCatching {
            JSONArray(context.assets.open("adhkar_$key.json").bufferedReader().use { it.readText() }).length()
        }.getOrNull()?.also { cache[key] = it }
    }
}
