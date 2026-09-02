package app.rafiqaldhikr.ui.screens.home

import app.rafiqaldhikr.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiq.domain.model.StreakInfo
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.repository.StreakRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import app.rafiqaldhikr.ui.utils.formatClock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import app.rafiqaldhikr.util.coordsOrNull

class HomeViewModel(
    /*  السياقُ لقراءة الموارد وحدَها.
     *
     *  كانت أسماءُ الصلوات والشهورِ الهجرية والتحيّاتُ مكتوبةً عربيةً في
     *  هذا الـViewModel — فتصل الشاشةَ نصّاً جاهزاً لا يمرّ بمترجم.
     *  و`applicationContext` لا يُسرَّب: عمرُه عمرُ التطبيق. */
    private val context: android.content.Context,
    private val progressRepo: ProgressRepository,
    private val prefsRepo:    PrefsRepository,
    private val streakRepo:   StreakRepository,
    private val getPrayerTimes: app.rafiq.domain.usecase.GetPrayerTimesUseCase
) : ViewModel() {

    // ═══ Prayer UI Model ═══
    data class PrayerUi(
        val ar: String,
        val en: String,
        val time: String,
        val timeMillis: Long = 0L,
        val done: Boolean = false,
        val active: Boolean = false
    )

    // ═══ UI State ═══
    data class UiState(
        val greeting:        String            = "",
        val hijriDate:       String            = "",
        val streak:          StreakInfo         = StreakInfo(0L, 0L, ""),
        val todayProgress:   DailyProgressInfo? = null,
        val prayerMethod:    String            = "mwl",
        val prayers:         List<PrayerUi>    = emptyList(),
        val nextPrayerName:  String            = "",
        val nextPrayerTime:  String            = "",
        val nextPrayerMillis: Long             = 0L,
        val prevPrayerMillis: Long             = 0L,
        val countdown:       String            = "",
        val wirdCurrent:     Int               = 0,
        val wirdTotal:       Int               = 1000,
        val wirdPercent:     Int               = 0,
        val isLoading:       Boolean           = true,
        // 0.0/0.0 = لا موقع. hasLocation هو الحكم، لا الإحداثية نفسها.
        val lat:             Double            = 0.0,
        val lng:             Double            = 0.0,
        val hasLocation:     Boolean           = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // يُبعث عند حلول وقت الصلاة لإعادة حساب المواقيت والصلاة التالية
    private val reloadTrigger = MutableStateFlow(0)

    init {
        load()
        startCountdownTimer()
    }

    private fun load() {
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()

            combine(
                progressRepo.getByDate(today),
                streakRepo.getStreak(),
                prefsRepo.getPrefs(),
                reloadTrigger
            ) { progress, streak, prefs, _ ->

                // ═══ حساب مواقيت الصلاة ═══
                val here = coordsOrNull(prefs.lastKnownLat, prefs.lastKnownLng)
                val lat = here?.lat ?: 0.0
                val lng = here?.lng ?: 0.0
                val prayerResult = if (here == null) null else try {
                    val res = getPrayerTimes(
                        lat = lat,
                        lng = lng,
                        method = prefs.prayerMethod,
                        elevation = prefs.elevation,
                        madhab = prefs.madhab,
                        fajrOffset = prefs.fajrOffset,
                        dhuhrOffset = prefs.dhuhrOffset,
                        asrOffset = prefs.asrOffset,
                        maghribOffset = prefs.maghribOffset,
                        ishaOffset = prefs.ishaOffset
                    )
                    if (res is app.rafiq.domain.model.RafiqResult.Success) res.data else null
                } catch (_: Exception) { null }

                val prayers = buildPrayerList(prayerResult)
                var nextPrayer = prayers.firstOrNull { it.active }
                // آخر صلاة مضى وقتها — أساس شريط تقدم الوقت المتبقي
                val prevMillis = prayers.lastOrNull { it.done && it.timeMillis > 0 }?.timeMillis ?: 0L

                // ═══ إذا انتهت جميع صلوات اليوم → حساب فجر الغد ═══
                var nextMillis = nextPrayer?.timeMillis ?: 0L
                if (nextPrayer == null && prayerResult != null) {
                    val tomorrowResult = try {
                        val res = getPrayerTimes.getForTomorrow(
                            lat = lat,
                            lng = lng,
                            method = prefs.prayerMethod,
                            elevation = prefs.elevation,
                            madhab = prefs.madhab,
                            fajrOffset = prefs.fajrOffset,
                            dhuhrOffset = prefs.dhuhrOffset,
                            asrOffset = prefs.asrOffset,
                            maghribOffset = prefs.maghribOffset,
                            ishaOffset = prefs.ishaOffset
                        )
                        if (res is app.rafiq.domain.model.RafiqResult.Success) res.data else null
                    } catch (_: Exception) { null }
                    if (tomorrowResult != null) {
                        nextPrayer = PrayerUi(
                            ar = context.getString(R.string.fajr),
                            en = "Fajr",
                            time = formatMillisToTime(tomorrowResult.fajr),
                            timeMillis = tomorrowResult.fajr,
                            done = false,
                            active = true
                        )
                        nextMillis = tomorrowResult.fajr
                    }
                }

                // ═══ حساب تقدم الورد ═══
                val wirdCurrent = calculateWirdProgress(progress)
                val wirdTotal = 1000
                val wirdPercent = if (wirdTotal > 0) (wirdCurrent * 100 / wirdTotal).coerceIn(0, 100) else 0

                UiState(
                    greeting        = buildGreeting(),
                    hijriDate       = calculateHijriDate(prefs.hijriOffset),
                    streak          = streak,
                    todayProgress   = progress,
                    prayerMethod    = prefs.prayerMethod,
                    prayers         = prayers,
                    nextPrayerName  = nextPrayer?.ar ?: context.getString(R.string.fajr),
                    nextPrayerTime  = nextPrayer?.time ?: "—",
                    nextPrayerMillis = nextMillis,
                    prevPrayerMillis = prevMillis,
                    countdown       = "",
                    wirdCurrent     = wirdCurrent,
                    wirdTotal       = wirdTotal,
                    wirdPercent     = wirdPercent,
                    isLoading       = false,
                    lat             = lat,
                    lng             = lng,
                    hasLocation     = here != null
                )
            }.collect { _uiState.value = it }
        }
    }

    // ═══ العد التنازلي — يتحدث كل ثانية، ويتوقف تلقائياً حين لا مراقب (خلفية) ═══
    private fun startCountdownTimer() {
        viewModelScope.launch {
            var lastReloadTarget = 0L
            while (true) {
                if (_uiState.subscriptionCount.value == 0) {
                    _uiState.subscriptionCount.first { it > 0 }
                }
                val current = _uiState.value
                val targetMillis = current.nextPrayerMillis
                if (targetMillis > 0) {
                    val now = System.currentTimeMillis()
                    val diff = targetMillis - now
                    if (diff > 0) {
                        val hours = diff / 3_600_000
                        val mins = (diff % 3_600_000) / 60_000
                        val secs = (diff % 60_000) / 1_000
                        val countdownStr = "%02d:%02d:%02d".format(hours, mins, secs)
                        _uiState.value = current.copy(countdown = countdownStr)
                    } else {
                        _uiState.value = current.copy(countdown = "00:00:00")
                        // وقت الصلاة حان — إعادة حساب الصلاة التالية (مرة واحدة لكل موعد)
                        if (targetMillis != lastReloadTarget) {
                            lastReloadTarget = targetMillis
                            reloadTrigger.value++
                        }
                    }
                }
                delay(1_000)
            }
        }
    }

    // ═══ بناء قائمة الصلوات من PrayerTimesResult ═══
    private fun buildPrayerList(result: PrayerTimesResult?): List<PrayerUi> {
        if (result == null) return defaultPrayers()

        val now = System.currentTimeMillis()
        val prayers = listOf(
            Triple(context.getString(R.string.fajr), "Fajr", result.fajr),
            Triple(context.getString(R.string.sunrise), "Sunrise", result.sunrise),
            Triple(context.getString(R.string.dhuhr), "Dhuhr", result.dhuhr),
            Triple(context.getString(R.string.asr), "Asr", result.asr),
            Triple(context.getString(R.string.maghrib), "Maghrib", result.maghrib),
            Triple(context.getString(R.string.isha), "Isha", result.isha)
        )

        // أول صلاة لم يمر وقتها = الصلاة النشطة
        var foundActive = false
        return prayers.map { (ar, en, millis) ->
            val passed = millis < now
            val active = !passed && !foundActive
            if (active) foundActive = true
            PrayerUi(
                ar = ar, en = en,
                time = formatMillisToTime(millis),
                timeMillis = millis,
                done = passed,
                active = active
            )
        }
    }

    private fun defaultPrayers(): List<PrayerUi> = listOf(
        PrayerUi(context.getString(R.string.fajr), "Fajr", "—"),
        PrayerUi(context.getString(R.string.sunrise), "Sunrise", "—"),
        PrayerUi(context.getString(R.string.dhuhr), "Dhuhr", "—"),
        PrayerUi(context.getString(R.string.asr), "Asr", "—"),
        PrayerUi(context.getString(R.string.maghrib), "Maghrib", "—"),
        PrayerUi(context.getString(R.string.isha), "Isha", "—")
    )

    // ═══ حساب تقدم الورد اليومي ═══
    private fun calculateWirdProgress(progress: DailyProgressInfo?): Int {
        if (progress == null) return 0
        var score = 0
        if (progress.morningDone) score += 200
        if (progress.eveningDone) score += 200
        score += (progress.quranPages * 50).toInt().coerceAtMost(300)
        score += progress.tasbeehCount.toInt().coerceAtMost(200)
        score += (progress.prayersLogged * 20).toInt().coerceAtMost(100)
        return score.coerceAtMost(1000)
    }

    // ═══ التحية — مصدر واحد للحقيقة ═══
    private fun buildGreeting(): String {
        val hour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when (hour) {
            in 5..11  -> context.getString(R.string.greet_time_morning)
            in 12..16 -> context.getString(R.string.greet_time_day)
            in 17..20 -> context.getString(R.string.greet_time_evening)
            else      -> context.getString(R.string.greet_time_night)
        }
    }

    // ═══ التاريخ الهجري (أم القرى عبر ICU — دقيق) ═══
    private fun calculateHijriDate(offset: Long): String {
        val monthNames = context.resources.getStringArray(R.array.hijri_months).toList()
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val cal = android.icu.util.IslamicCalendar().apply {
                    calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
                    timeInMillis = System.currentTimeMillis() + offset * 86_400_000L
                }
                val day   = cal.get(android.icu.util.IslamicCalendar.DAY_OF_MONTH)
                val month = cal.get(android.icu.util.IslamicCalendar.MONTH)
                val year  = cal.get(android.icu.util.IslamicCalendar.YEAR)
                context.getString(R.string.hijri_date, day.toString(), monthNames[month], year.toString())
            } else {
                // أجهزة API 23: تقدير حسابي تقريبي
                approximateHijri(offset, monthNames)
            }
        } catch (_: Exception) {
            context.getString(R.string.hijri_unknown)
        }
    }

    private fun approximateHijri(offset: Long, monthNames: List<String>): String {
        // أم القرى: 1 محرم 1446 = 7 يوليو 2024
        val umAlQuraEpoch = Calendar.getInstance().apply {
            set(2024, Calendar.JULY, 7, 0, 0, 0)
        }.timeInMillis
        val daysDiff = ((System.currentTimeMillis() - umAlQuraEpoch) / 86_400_000) + offset
        val hijriMonth = ((daysDiff / 29.53) % 12).toInt()
        val hijriDay = ((daysDiff % 29.53) + 1).toInt().coerceIn(1, 30)
        val hijriYear = 1446 + (daysDiff / 354.36).toInt()
        return context.getString(R.string.hijri_date, hijriDay.toString(), monthNames[hijriMonth], hijriYear.toString())
    }

    // ═══ أدوات مساعدة ═══
    /*  كانت `SimpleDateFormat("h:mm")` بلا علامة صباحٍ ولا مساء — فالفجر
     *  ٥:٠٠ والعصر ٥:٠٠ سواء، ولا يعرف القارئ أيَّهما «الصلاة القادمة».
     *  والصيغةُ الآن واحدةٌ في التطبيق كلِّه: `formatClock`.  */
    private fun formatMillisToTime(millis: Long): String =
        formatClock(millis, arabic = true)

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            prefsRepo.updateLocation("", lat, lng)
        }
    }
}
