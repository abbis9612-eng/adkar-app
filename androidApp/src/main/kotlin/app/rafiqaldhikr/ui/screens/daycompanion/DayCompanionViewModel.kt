package app.rafiqaldhikr.ui.screens.daycompanion

import androidx.lifecycle.ViewModel
import app.rafiqaldhikr.util.coordsOrNull
import app.rafiqaldhikr.util.isFriday
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
        val title:       String,
        /** اسمٌ من كلمة واحدة لصفّ اليوم في الرئيسية — تسعةٌ منها تتّسع في سطر. */
        val short:       String,
        val description: String,
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
        val timeLabel:   String,          // «بعد الفجر حتى الشروق»
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
                title = "الاستيقاظ",
                short = "الاستيقاظ",
                description = "«الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور» — والسواك",
                virtue = "هدي النبي ﷺ عند الاستيقاظ — رواه البخاري",
                source = "رواه البخاري",
                timeLabel = "قبل الفجر",
                startMillis = t.fajr - 90 * 60_000L, endMillis = t.fajr,
                route = null,
            ),
            StationUi(
                id = "fajr_morning",
                title = "الفجر وأذكار الصباح",
                short = "الفجر",
                description = "صلاة الفجر ثم أذكار الصباح حتى طلوع الشمس",
                virtue = "«من صلى الغداة في جماعة ثم قعد يذكر الله حتى تطلع الشمس ثم صلى ركعتين كانت له كأجر حجة وعمرة تامة تامة تامة» — الترمذي (حسن)",
                source = "الترمذي · حسن",
                timeLabel = "من الفجر إلى الشروق",
                startMillis = t.fajr, endMillis = t.sunrise,
                route = "dhikr_reading/morning",
            ),
            StationUi(
                id = "duha",
                title = "صلاة الضحى",
                short = "الضحى",
                description = "ركعتان تجزئان عن صدقة عن كل مفصل من مفاصلك",
                virtue = "«يصبح على كل سُلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى» — رواه مسلم",
                source = "رواه مسلم",
                timeLabel = "من بعد الشروق إلى قبيل الظهر",
                startMillis = t.sunrise + 20 * 60_000L, endMillis = t.dhuhr - 10 * 60_000L,
                route = null,
            ),
            StationUi(
                id = "dhuhr",
                title = "الظهر وأذكار بعد الصلاة",
                short = "الظهر",
                description = "الصلاة ثم الاستغفار والتسبيح 33/33/34 وآية الكرسي",
                virtue = "«من سبّح الله دبر كل صلاة... غُفرت خطاياه وإن كانت مثل زبد البحر» — رواه مسلم",
                source = "رواه مسلم",
                timeLabel = "من الظهر إلى العصر",
                startMillis = t.dhuhr, endMillis = t.asr,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "asr_evening",
                title = "العصر وأذكار المساء",
                short = "العصر",
                description = "صلاة العصر ثم أذكار المساء قبل الغروب",
                virtue = "اختار ابن القيم في الوابل الصيّب أن وقت أذكار المساء بين العصر والغروب",
                source = "اختيار ابن القيّم · الوابل الصيّب",
                timeLabel = "من العصر إلى المغرب",
                startMillis = t.asr, endMillis = t.maghrib,
                route = "dhikr_reading/evening",
            ),
            StationUi(
                id = "maghrib",
                title = "المغرب وأذكار بعد الصلاة",
                short = "المغرب",
                description = "الصلاة وأذكارها" + if (friday) " — وأكثر من الدعاء فآخر ساعة من الجمعة ساعة إجابة" else "",
                virtue = "«لا مانع لما أعطيت ولا معطي لما منعت» — متفق عليه",
                source = "متفق عليه",
                timeLabel = "من المغرب إلى العشاء",
                startMillis = t.maghrib, endMillis = t.isha,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "isha",
                title = "العشاء والوتر",
                short = "العشاء",
                description = "صلاة العشاء ثم الوتر ولو بركعة",
                virtue = "«اجعلوا آخر صلاتكم بالليل وتراً» — متفق عليه",
                source = "متفق عليه",
                timeLabel = "بعد العشاء",
                startMillis = t.isha, endMillis = sleepStart,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "sleep",
                title = "أذكار النوم",
                short = "النوم",
                description = "الوضوء، آية الكرسي، الإخلاص والمعوذتان، خواتيم البقرة، والتسبيح",
                virtue = "«إذا أويت إلى فراشك فاقرأ آية الكرسي... لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح» — رواه البخاري",
                source = "رواه البخاري",
                timeLabel = "عند النوم",
                startMillis = sleepStart, endMillis = dayEnd,
                route = "dhikr_reading/sleep",
            ),
        ) + if (friday) listOf(
            StationUi(
                id = "friday_kahf",
                title = "سورة الكهف والصلاة على النبي ﷺ",
                short = "الكهف",
                description = "قراءة سورة الكهف والإكثار من الصلاة على النبي ﷺ يوم الجمعة",
                virtue = "«من قرأ سورة الكهف في يوم الجمعة أضاء له من النور ما بين الجمعتين» — رواه الحاكم والبيهقي (صحيح)",
                source = "الحاكم والبيهقي · صحيح",
                timeLabel = "طوال يوم الجمعة",
                startMillis = t.fajr, endMillis = t.isha,
                // صفحةُ الكهف في المصحف المدنيّ. وكان هنا "quran_reading/18"
                // — مسارٌ لا وجود له في الرسم البياني منذ حُذفت شاشةُ القراءة
                // بالسورة، فكان كلُّ يوم جمعةٍ ينتهي بانهيار عند فتح المحطّة.
                route = RafiqRoute.Mushaf.atPage(KAHF_PAGE),
            )
        ) else emptyList()
    }
}
