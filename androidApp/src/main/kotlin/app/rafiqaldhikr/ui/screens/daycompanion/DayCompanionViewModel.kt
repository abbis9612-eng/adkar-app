package app.rafiqaldhikr.ui.screens.daycompanion

import androidx.lifecycle.ViewModel
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
import kotlinx.datetime.DayOfWeek
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

    enum class StationStatus { UPCOMING, ACTIVE, DONE, PASSED }

    data class StationUi(
        val id:          String,
        val emoji:       String,
        val title:       String,
        val description: String,
        val virtue:      String,          // الفضل الوارد بدليله
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
                val lat = prefs.lastKnownLat.takeIf { it != 0.0 } ?: 35.5558
                val lng = prefs.lastKnownLng.takeIf { it != 0.0 } ?: 45.4436
                val times = when (val r = getPrayerTimes(
                    lat, lng, prefs.prayerMethod,
                    elevation = prefs.elevation, madhab = prefs.madhab,
                    fajrOffset = prefs.fajrOffset, dhuhrOffset = prefs.dhuhrOffset,
                    asrOffset = prefs.asrOffset, maghribOffset = prefs.maghribOffset,
                    ishaOffset = prefs.ishaOffset
                )) {
                    is RafiqResult.Success -> r.data
                    else -> null
                } ?: return@combine UiState(isLoading = true)

                val now = System.currentTimeMillis()
                val dayOfWeek = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek

                // إتمام تلقائي: أذكار الصباح/المساء من سجل التقدم الموجود
                val auto = buildSet {
                    if (progress?.morningDone == true) add("fajr_morning")
                    if (progress?.eveningDone == true) add("asr_evening")
                }
                val allDone = completed + auto

                val stations = buildStations(times, dayOfWeek == DayOfWeek.FRIDAY).map { st ->
                    val status = when {
                        st.id in allDone                      -> StationStatus.DONE
                        now in st.startMillis..st.endMillis   -> StationStatus.ACTIVE
                        now > st.endMillis                    -> StationStatus.PASSED
                        else                                  -> StationStatus.UPCOMING
                    }
                    st.copy(status = status)
                }

                UiState(
                    stations   = stations,
                    nowStation = stations.firstOrNull { it.status == StationStatus.ACTIVE }
                        ?: stations.firstOrNull { it.status == StationStatus.UPCOMING },
                    isFriday   = dayOfWeek == DayOfWeek.FRIDAY,
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
                id = "wake", emoji = "🌅",
                title = "الاستيقاظ",
                description = "«الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور» — والسواك",
                virtue = "هدي النبي ﷺ عند الاستيقاظ — رواه البخاري",
                timeLabel = "قبل الفجر",
                startMillis = t.fajr - 90 * 60_000L, endMillis = t.fajr,
                route = null,
            ),
            StationUi(
                id = "fajr_morning", emoji = "☀️",
                title = "الفجر وأذكار الصباح",
                description = "صلاة الفجر ثم أذكار الصباح حتى طلوع الشمس",
                virtue = "«من صلى الغداة في جماعة ثم قعد يذكر الله حتى تطلع الشمس ثم صلى ركعتين كانت له كأجر حجة وعمرة تامة تامة تامة» — الترمذي (حسن)",
                timeLabel = "من الفجر إلى الشروق",
                startMillis = t.fajr, endMillis = t.sunrise,
                route = "dhikr_reading/morning",
            ),
            StationUi(
                id = "duha", emoji = "🌤",
                title = "صلاة الضحى",
                description = "ركعتان تجزئان عن صدقة عن كل مفصل من مفاصلك",
                virtue = "«يصبح على كل سُلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى» — رواه مسلم",
                timeLabel = "من بعد الشروق إلى قبيل الظهر",
                startMillis = t.sunrise + 20 * 60_000L, endMillis = t.dhuhr - 10 * 60_000L,
                route = null,
            ),
            StationUi(
                id = "dhuhr", emoji = "🕌",
                title = "الظهر وأذكار بعد الصلاة",
                description = "الصلاة ثم الاستغفار والتسبيح 33/33/34 وآية الكرسي",
                virtue = "«من سبّح الله دبر كل صلاة... غُفرت خطاياه وإن كانت مثل زبد البحر» — رواه مسلم",
                timeLabel = "من الظهر إلى العصر",
                startMillis = t.dhuhr, endMillis = t.asr,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "asr_evening", emoji = "🌇",
                title = "العصر وأذكار المساء",
                description = "صلاة العصر ثم أذكار المساء قبل الغروب",
                virtue = "اختار ابن القيم في الوابل الصيّب أن وقت أذكار المساء بين العصر والغروب",
                timeLabel = "من العصر إلى المغرب",
                startMillis = t.asr, endMillis = t.maghrib,
                route = "dhikr_reading/evening",
            ),
            StationUi(
                id = "maghrib", emoji = "🌆",
                title = "المغرب وأذكار بعد الصلاة",
                description = "الصلاة وأذكارها" + if (friday) " — وأكثر من الدعاء فآخر ساعة من الجمعة ساعة إجابة" else "",
                virtue = "«لا مانع لما أعطيت ولا معطي لما منعت» — متفق عليه",
                timeLabel = "من المغرب إلى العشاء",
                startMillis = t.maghrib, endMillis = t.isha,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "isha", emoji = "🌃",
                title = "العشاء والوتر",
                description = "صلاة العشاء ثم الوتر ولو بركعة",
                virtue = "«اجعلوا آخر صلاتكم بالليل وتراً» — متفق عليه",
                timeLabel = "بعد العشاء",
                startMillis = t.isha, endMillis = sleepStart,
                route = "dhikr_reading/prayer",
            ),
            StationUi(
                id = "sleep", emoji = "🌙",
                title = "أذكار النوم",
                description = "الوضوء، آية الكرسي، الإخلاص والمعوذتان، خواتيم البقرة، والتسبيح",
                virtue = "«إذا أويت إلى فراشك فاقرأ آية الكرسي... لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح» — رواه البخاري",
                timeLabel = "عند النوم",
                startMillis = sleepStart, endMillis = dayEnd,
                route = "dhikr_reading/sleep",
            ),
        ) + if (friday) listOf(
            StationUi(
                id = "friday_kahf", emoji = "📖",
                title = "سورة الكهف والصلاة على النبي ﷺ",
                description = "قراءة سورة الكهف والإكثار من الصلاة على النبي ﷺ يوم الجمعة",
                virtue = "«من قرأ سورة الكهف في يوم الجمعة أضاء له من النور ما بين الجمعتين» — رواه الحاكم والبيهقي (صحيح)",
                timeLabel = "طوال يوم الجمعة",
                startMillis = t.fajr, endMillis = t.isha,
                route = "quran_reading/18",
            )
        ) else emptyList()
    }
}
