package app.rafiqaldhikr.ui.screens.daycompanion

import androidx.lifecycle.ViewModel
import app.rafiqaldhikr.util.coordsOrNull
import app.rafiqaldhikr.util.isFriday
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.RafiqResult
import app.rafiq.domain.repository.DayCompanionRepository
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.usecase.GetPrayerTimesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * «رفيق اليوم» — يوم المسلم من الاستيقاظ إلى النوم على مراسي الصلوات الخمس.
 *
 * المنهج: الصلوات الخمس مراسٍ يومية ثابتة (Anchors — BJ Fogg)، وكل محطة
 * عمل مأثور موثق. القاعدة: «أحب الأعمال إلى الله أدومها وإن قل» (متفق عليه).
 */
class DayCompanionViewModel(
    private val prefsRepo:      PrefsRepository,
    private val progressRepo:   ProgressRepository,
    private val companionRepo:  DayCompanionRepository,
    private val getPrayerTimes: GetPrayerTimesUseCase,
) : ViewModel() {

    private companion object {
        /** أوّلُ صفحاتِ الكهف في المصحف المدنيّ — مرساةُ محطّة الجمعة. */
        const val KAHF_PAGE = 293
    }

    enum class StationStatus { UPCOMING, ACTIVE, DONE, PASSED }

    data class StationUi(
        val id:          String,
        /*  العنوانُ والاسمُ القصيرُ والوصفُ والتوقيتُ مراجعُ موارد لا نصوص:
         *  كانت أربعتُها مكتوبةً عربيةً هنا لعشرِ محطّات — فميزةُ «رفيق
         *  اليوم» كلُّها لا تُترجَم، وهي بطلةُ الشاشة الرئيسية.
         *
         *  و[virtue] و[source] يبقيان نصّاً عربياً: هما حديثٌ وتخريجُه،
         *  وترجمةُ الحديث تفسيرٌ له لا نقل — وقاعدةُ المشروع أن لا يُصاغ
         *  نصٌّ دينيٌّ ولا يُعاد صوغُه. فيُعرضان بالعربية في اللغتين، وهو
         *  الصدق. */
        @androidx.annotation.StringRes val title: Int,
        /** اسمٌ من كلمة واحدة لصفّ اليوم في الرئيسية — تسعةٌ منها تتّسع في سطر. */
        @androidx.annotation.StringRes val short: Int,
        @androidx.annotation.StringRes val description: Int,
        val virtue:      String,          // الفضل الوارد بدليله
        /**
         * تخريجُ [virtue] وحده — شارةُ المصدر في بطاقة الميقات.
         *
         * حقلٌ صريحٌ لا اقتطاعٌ من [virtue]: نصُّ الفضل ليس كلُّه حديثاً
         * (فضلُ أذكار المساء اختيارُ ابن القيّم لا حديثٌ مرفوع)، ولا كلُّه
         * ينتهي بفاصلةٍ يُعتمد عليها. وشارةُ مصدرٍ مشتقّةٌ بالتحليل النصّي
         * تكذب يوماً، والكذبُ هنا في الإسناد لا في الواجهة.
         */
        val source:      String,
        @androidx.annotation.StringRes val timeLabel: Int,
        val startMillis: Long,
        val endMillis:   Long,
        val route:       String?,         // وجهة «ابدأ» — فئة أذكار غالباً
        val status:      StationStatus = StationStatus.UPCOMING,
    )

    data class UiState(
        val stations:   List<StationUi> = emptyList(),
        val nowStation: StationUi?      = null,
        val isFriday:   Boolean         = false,
        val doneCount:  Int             = 0,
        val isLoading:  Boolean         = true,
        /** لا إحداثيات محفوظة — محطّات اليوم موقوتة بالصلاة فلا تُبنى بدونها. */
        val needsLocation: Boolean      = false,
        /**
         * ما سجّله صاحبُه فعلاً — لا ما مرَّ وقتُه.
         *
         * التطبيقُ يعرف أنّ الوقت مضى، ولا يعرف أنّه صلّى. وكتابةُ «تمّت»
         * لمجرّد انقضاء الوقت شهادةٌ له بعبادةٍ لم تُسجَّل — وهي كذبٌ عليه
         * في أخصِّ ما عنده. فما مضى يُكتب «مضت»، و«تمّت» لهؤلاء وحدهم.
         */
        val completedIds: Set<String>   = emptySet(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // مهم: يُعلن قبل init — كتلة init تستخدمه عبر load()
    private val refreshTrigger = MutableStateFlow(0)

    private val today: String
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    init {
        load()
        // تحديث الحالة النشطة كل دقيقة — يتوقف تلقائياً حين لا مراقب (خلفية)
        viewModelScope.launch {
            while (true) {
                if (_uiState.subscriptionCount.value == 0) {
                    _uiState.subscriptionCount.first { it > 0 }
                }
                delay(60_000)
                refreshTrigger.value++
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            combine(
                prefsRepo.getPrefs(),
                progressRepo.getByDate(today),
                companionRepo.getCompletedStations(today),
                refreshTrigger,
            ) { prefs, progress, completed, _ ->
                // محطّات اليوم كلّها موقوتة بأوقات الصلاة. بلا موقع لا محطّات —
                // والورقة تطلب الموقع بدل أن تعرض جدول مدينةٍ ليست مدينته.
                val here = coordsOrNull(prefs.lastKnownLat, prefs.lastKnownLng)
                    ?: return@combine UiState(isLoading = false, needsLocation = true)
                val times = when (val r = getPrayerTimes(
                    here.lat, here.lng, prefs.prayerMethod,
                    elevation = prefs.elevation, madhab = prefs.madhab,
                    fajrOffset = prefs.fajrOffset, dhuhrOffset = prefs.dhuhrOffset,
                    asrOffset = prefs.asrOffset, maghribOffset = prefs.maghribOffset,
                    ishaOffset = prefs.ishaOffset
                )) {
                    is RafiqResult.Success -> r.data
                    else -> null
                } ?: return@combine UiState(isLoading = true)

                val now = System.currentTimeMillis()
                val todayDate = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date

                // إتمام تلقائي: أذكار الصباح/المساء من سجل التقدم الموجود
                val auto = buildSet {
                    if (progress?.morningDone == true) add("fajr_morning")
                    if (progress?.eveningDone == true) add("asr_evening")
                }
                val allDone = completed + auto

                val stations = buildStations(times, todayDate.isFriday()).map { st ->
                    val status = when {
                        st.id in allDone                      -> StationStatus.DONE
                        now in st.startMillis..st.endMillis   -> StationStatus.ACTIVE
                        now > st.endMillis                    -> StationStatus.PASSED
                        else                                  -> StationStatus.UPCOMING
                    }
                    st.copy(status = status)
                }

                UiState(
                    completedIds = allDone,
                    stations   = stations,
                    nowStation = stations.firstOrNull { it.status == StationStatus.ACTIVE }
                        ?: stations.firstOrNull { it.status == StationStatus.UPCOMING },
                    isFriday   = todayDate.isFriday(),
                    doneCount  = stations.count { it.status == StationStatus.DONE },
                    isLoading  = false,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun completeStation(id: String) {
        viewModelScope.launch {
            companionRepo.completeStation(today, id)
        }
    }

    private fun buildStations(t: app.rafiq.domain.model.PrayerTimesResult, friday: Boolean): List<StationUi> {
        val sleepStart = t.isha + 60 * 60_000L      // بعد العشاء بساعة
        val dayEnd     = t.isha + 5 * 3600_000L     // نهاية نافذة النوم للعرض

        return listOf(
            StationUi(
                id = "wake",
                title = R.string.st_wake_t,
                short = R.string.st_wake_s,
                description = R.string.st_wake_d,
                virtue = "هدي النبي ﷺ عند الاستيقاظ — رواه البخاري",
                source = "رواه البخاري",
                timeLabel = R.string.st_wake_time,
                startMillis = t.fajr - 90 * 60_000L, endMillis = t.fajr,
                route = null,
            ),
            StationUi(
                id = "fajr_morning",
                title = R.string.st_fajr_t,
                short = R.string.st_fajr_s,
                description = R.string.st_fajr_d,
                virtue = "«من صلى الغداة في جماعة ثم قعد يذكر الله حتى تطلع الشمس ثم صلى ركعتين كانت له كأجر حجة وعمرة تامة تامة تامة» — الترمذي (حسن)",
                source = "الترمذي · حسن",
                timeLabel = R.string.st_fajr_time,
                startMillis = t.fajr, endMillis = t.sunrise,
                route = "dhikr_reading/morning",
            ),
            StationUi(
                id = "duha",
                title = R.string.st_duha_t,
                short = R.string.st_duha_s,
                description = R.string.st_duha_d,
                virtue = "«يصبح على كل سُلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى» — رواه مسلم",
                source = "رواه مسلم",
                timeLabel = R.string.st_duha_time,
                startMillis = t.sunrise + 20 * 60_000L, endMillis = t.dhuhr - 10 * 60_000L,
                route = null,
            ),
            StationUi(
                id = "dhuhr",
                title = R.string.st_dhuhr_t,
                short = R.string.st_dhuhr_s,
                description = R.string.st_dhuhr_d,
                virtue = "«من سبّح الله دبر كل صلاة... غُفرت خطاياه وإن كانت مثل زبد البحر» — رواه مسلم",
                source = "رواه مسلم",
                timeLabel = R.string.st_dhuhr_time,
                startMillis = t.dhuhr, endMillis = t.asr,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "asr_evening",
                title = R.string.st_asr_t,
                short = R.string.st_asr_s,
                description = R.string.st_asr_d,
                virtue = "اختار ابن القيم في الوابل الصيّب أن وقت أذكار المساء بين العصر والغروب",
                source = "اختيار ابن القيّم · الوابل الصيّب",
                timeLabel = R.string.st_asr_time,
                startMillis = t.asr, endMillis = t.maghrib,
                route = "dhikr_reading/evening",
            ),
            StationUi(
                id = "maghrib",
                title = R.string.st_maghrib_t,
                short = R.string.st_maghrib_s,
                // الجمعةُ لها وصفٌ خاصّ: آخرُ ساعةٍ منها ساعةُ إجابة.
                description = if (friday) R.string.st_maghrib_d_friday else R.string.st_maghrib_d,
                virtue = "«لا مانع لما أعطيت ولا معطي لما منعت» — متفق عليه",
                source = "متفق عليه",
                timeLabel = R.string.st_maghrib_time,
                startMillis = t.maghrib, endMillis = t.isha,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "isha",
                title = R.string.st_isha_t,
                short = R.string.st_isha_s,
                description = R.string.st_isha_d,
                virtue = "«اجعلوا آخر صلاتكم بالليل وتراً» — متفق عليه",
                source = "متفق عليه",
                timeLabel = R.string.st_isha_time,
                startMillis = t.isha, endMillis = sleepStart,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "sleep",
                title = R.string.st_sleep_t,
                short = R.string.st_sleep_s,
                description = R.string.st_sleep_d,
                virtue = "«إذا أويت إلى فراشك فاقرأ آية الكرسي... لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح» — رواه البخاري",
                source = "رواه البخاري",
                timeLabel = R.string.st_sleep_time,
                startMillis = sleepStart, endMillis = dayEnd,
                route = "dhikr_reading/sleep",
            ),
        ) + if (friday) listOf(
            StationUi(
                id = "friday_kahf",
                title = R.string.st_kahf_t,
                short = R.string.st_kahf_s,
                description = R.string.st_kahf_d,
                virtue = "«من قرأ سورة الكهف في يوم الجمعة أضاء له من النور ما بين الجمعتين» — رواه الحاكم والبيهقي (صحيح)",
                source = "الحاكم والبيهقي · صحيح",
                timeLabel = R.string.st_kahf_time,
                startMillis = t.fajr, endMillis = t.isha,
                // صفحةُ الكهف في المصحف المدنيّ. وكان هنا "quran_reading/18"
                // — مسارٌ لا وجود له في الرسم البياني منذ حُذفت شاشةُ القراءة
                // بالسورة، فكان كلُّ يوم جمعةٍ ينتهي بانهيار عند فتح المحطّة.
                route = RafiqRoute.Mushaf.atPage(KAHF_PAGE),
            )
        ) else emptyList()
    }
}
