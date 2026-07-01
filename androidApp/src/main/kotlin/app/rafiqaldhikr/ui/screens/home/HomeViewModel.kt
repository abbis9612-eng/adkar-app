package app.rafiqaldhikr.ui.screens.home

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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(
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
        val countdown:       String            = "",
        val wirdCurrent:     Int               = 0,
        val wirdTotal:       Int               = 1000,
        val wirdPercent:     Int               = 0,
        val isLoading:       Boolean           = true,
        val lat:             Double            = 35.5558, // السليمانية كاحتياطي
        val lng:             Double            = 45.4436, // السليمانية كاحتياطي
        val hasLocation:     Boolean           = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
                prefsRepo.getPrefs()
            ) { progress, streak, prefs ->

                // ═══ حساب مواقيت الصلاة ═══
                val lat = prefs.lastKnownLat.takeIf { it != 0.0 } ?: 35.5558
                val lng = prefs.lastKnownLng.takeIf { it != 0.0 } ?: 45.4436
                val prayerResult = try {
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
                            ar = "الفجر",
                            en = "Fajr",
                            time = toArabicNumerals(formatMillisToTime(tomorrowResult.fajr)),
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
                    nextPrayerName  = nextPrayer?.ar ?: "الفجر",
                    nextPrayerTime  = nextPrayer?.time ?: "—",
                    nextPrayerMillis = nextMillis,
                    countdown       = "",
                    wirdCurrent     = wirdCurrent,
                    wirdTotal       = wirdTotal,
                    wirdPercent     = wirdPercent,
                    isLoading       = false,
                    lat             = lat,
                    lng             = lng,
                    hasLocation     = prefs.lastKnownLat != 0.0
                )
            }.collect { _uiState.value = it }
        }
    }

    // ═══ العد التنازلي — يتحدث كل ثانية ═══
    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
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
                        _uiState.value = current.copy(countdown = toArabicNumerals(countdownStr))
                    } else {
                        // وقت الصلاة حان — إعادة تحميل البيانات
                        _uiState.value = current.copy(countdown = toArabicNumerals("00:00:00"))
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
            Triple("الفجر", "Fajr", result.fajr),
            Triple("الشروق", "Sunrise", result.sunrise),
            Triple("الظهر", "Dhuhr", result.dhuhr),
            Triple("العصر", "Asr", result.asr),
            Triple("المغرب", "Maghrib", result.maghrib),
            Triple("العشاء", "Isha", result.isha)
        )

        // أول صلاة لم يمر وقتها = الصلاة النشطة
        var foundActive = false
        return prayers.map { (ar, en, millis) ->
            val passed = millis < now
            val active = !passed && !foundActive
            if (active) foundActive = true
            PrayerUi(
                ar = ar, en = en,
                time = toArabicNumerals(formatMillisToTime(millis)),
                timeMillis = millis,
                done = passed,
                active = active
            )
        }
    }

    private fun defaultPrayers(): List<PrayerUi> = listOf(
        PrayerUi("الفجر", "Fajr", "—"),
        PrayerUi("الشروق", "Sunrise", "—"),
        PrayerUi("الظهر", "Dhuhr", "—"),
        PrayerUi("العصر", "Asr", "—"),
        PrayerUi("المغرب", "Maghrib", "—"),
        PrayerUi("العشاء", "Isha", "—")
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
            in 5..11  -> "صباح الخير"
            in 12..16 -> "مساء الخير"
            in 17..20 -> "مساء النور"
            else      -> "طابت ليلتك"
        }
    }

    // ═══ تقدير التاريخ الهجري ═══
    private fun calculateHijriDate(offset: Long): String {
        return try {
            // تقدير بسيط بناءً على الفرق بين التقويمين
            // أم القرى: 1 محرم 1446 = 7 يوليو 2024
            val umAlQuraEpoch = Calendar.getInstance().apply {
                set(2024, Calendar.JULY, 7, 0, 0, 0)
            }.timeInMillis

            val now = System.currentTimeMillis()
            val daysDiff = ((now - umAlQuraEpoch) / 86_400_000) + offset
            val hijriMonth = ((daysDiff / 29.53) % 12).toInt()
            val hijriDay = ((daysDiff % 29.53) + 1).toInt().coerceIn(1, 30)
            val hijriYear = 1446 + (daysDiff / 354.36).toInt()

            val monthNames = listOf(
                "محرّم", "صفر", "ربيع الأول", "ربيع الآخر",
                "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
            )

            "${toArabicNumerals(hijriDay.toString())} ${monthNames[hijriMonth]} ${toArabicNumerals(hijriYear.toString())} هـ"
        } catch (_: Exception) {
            "— هـ"
        }
    }

    // ═══ أدوات مساعدة ═══
    private fun formatMillisToTime(millis: Long): String {
        val sdf = SimpleDateFormat("h:mm", Locale.US)
        return sdf.format(Date(millis))
    }

    private fun toArabicNumerals(str: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return str.map { if (it.isDigit()) arabicDigits[it - '0'] else it }.joinToString("")
    }

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            prefsRepo.updateLocation("", lat, lng)
        }
    }
}
