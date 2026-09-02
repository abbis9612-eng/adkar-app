package app.rafiqaldhikr.ui.screens.qibla

import android.hardware.SensorManager
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.lifecycle.ViewModel
import app.rafiqaldhikr.util.coordsOrNull
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.usecase.CalculateQiblaUseCase
import app.rafiqaldhikr.service.CompassManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** أكبرُ فرقٍ زاويٍّ داخل نافذة القراءات، مع مراعاة الالتفاف عند 360°. */
private fun angularSpread(values: Collection<Float>): Float {
    val ref = values.first()
    val rel = values.map { ((it - ref + 540f) % 360f) - 180f }
    return (rel.max() - rel.min())
}

/** المسافةُ إلى الكعبة على دائرةٍ عظمى — نصفُ قطر الأرض المتوسّط 6371.0088 كم. */
private fun greatCircleKm(lat: Double, lng: Double): Double {
    val p1 = Math.toRadians(lat)
    val p2 = Math.toRadians(21.4225)
    val dp = p2 - p1
    val dl = Math.toRadians(39.8262 - lng)
    val h = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
    return 2 * 6371.0088 * asin(sqrt(h))
}

class QiblaViewModel(
    private val calculateQibla: CalculateQiblaUseCase,
    private val compassManager: CompassManager,
    private val prefsRepo:      PrefsRepository
) : ViewModel() {

    data class UiState(
        val qiblaBearing:      Float   = 0f,
        val deviceHeading:     Float   = 0f,
        val rotationToQibla:   Float   = 0f,
        val isCompassAvailable: Boolean = true,
        val isLocationKnown:   Boolean  = false,
        val error:             String?  = null,
        /** المسافةُ إلى الكعبة بالكيلومترات — تُحسب مع الاتّجاه من الإحداثيات نفسها. */
        val distanceKm:        Int     = 0,
        /**
         * بوّابتا الثقة.
         *
         * شاشةُ قبلةٍ تجزم بلا تحفّظٍ أسوأُ من شاشةٍ تعتذر: المصلّي يستقبل ما
         * تقول. فلا يُعلَن الاتّجاه مطمئنّاً حتى تصدق الاثنتان.
         *
         * ولم تُضَف بوّابةٌ ثالثة لاستواء الهاتف رغم حُسنها في العرض: تحتاج
         * مقياسَ تسارعٍ لا يُقرأ هنا، وبوّابةٌ مزيَّنةٌ تُبطل معنى البوّابات.
         */
        val compassTrusted:    Boolean = false,
        val readingSteady:     Boolean = false,
    ) {
        val trustworthy: Boolean get() = compassTrusted && readingSteady
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.getPrefs().collect { prefs ->
                if (prefs == null) return@collect
                // بوصلة تشير إلى قبلة مدينةٍ أخرى أسوأ من بوصلة لا تشير —
                // isLocationKnown = false والشاشة تطلب الموقع.
                val here = coordsOrNull(prefs.lastKnownLat, prefs.lastKnownLng)
                if (here == null) {
                    _uiState.update { it.copy(isLocationKnown = false) }
                    return@collect
                }
                val bearing = calculateQibla(here.lat, here.lng)

                _uiState.update {
                    it.copy(
                        qiblaBearing = bearing,
                        distanceKm = greatCircleKm(here.lat, here.lng).roundToInt(),
                        isLocationKnown = true
                    )
                }

                /*  البوصلةُ لا تبدأ قبل معرفة المكان.
                 *
                 *  تصحيحُ الانحراف المغناطيسي يحتاج الإحداثيات، وبدونها
                 *  تُقرأ الإبرةُ من الشمال المغناطيسي بينما القبلةُ محسوبةٌ
                 *  من الحقيقي — فتنحرف بمقدار انحراف المكان.  */
                if (!compassStarted) {
                    compassStarted = true
                    startCompass(here.lat, here.lng)
                }
            }
        }
    }

    /** حتى لا تُطلق البوصلةُ مرّتين حين تنبعث التفضيلاتُ ثانيةً. */
    private var compassStarted = false

    private fun startCompass(lat: Double, lng: Double) {
        viewModelScope.launch {
            if (!compassManager.isAvailable) {
                _uiState.update { it.copy(isCompassAvailable = false) }
                return@launch
            }
            /*  ثباتُ القراءة يُقاس ولا يُدّعى: آخرُ اثنتي عشرة قراءةً،
                والمدى بينها بالدرجات. فإن جاوز 6° فالإبرةُ ترتجف — إمّا
                معدنٌ قريبٌ أو يدٌ تتحرّك — ولا يُعلَن اتّجاهٌ عليها. */
            val window = ArrayDeque<Float>()
            /*  المستشعرُ يعمل ما دامت الشاشةُ منظورة.
             *
             *  كان المجمِّعُ يعمل ما دام الـViewModel حيّاً — والـViewModel
             *  يعيش بعمر مدخل التنقّل، فيبقى مستشعرُ الدوران يُطلق قراءاتٍ
             *  والتطبيقُ في الخلفية. وهي نفسُ الحيلة المستعملة في مؤقّت
             *  `DayCompanionViewModel`: لا مشترِكَ فلا عمل.  */
            /*  الخطأُ يُعرَض ولا يُسقط التطبيق.
             *
             *  كان `.collect` عارياً: أيُّ نازلٍ في المستشعر أو في تدفّقه
             *  يخرج غيرَ ملتقَطٍ من `viewModelScope.launch` فيقتل العملية.
             *  ولا شيءَ يُعيد فتحَ التطبيق بعدها، لأنّ أندرويد يستعيد
             *  الشاشةَ نفسَها فيسقط ثانيةً.
             *
             *  وشاشةُ القبلة تعرف كيف تقول «لا بوصلة في جهازك» — وهي
             *  الرسالةُ الصحيحة حين تخفق البوصلةُ لأيّ سبب. */
            compassManager.getReadingFlow(lat, lng)
                .catch { _uiState.update { s -> s.copy(isCompassAvailable = false) } }
                .collect { reading ->
                if (_uiState.subscriptionCount.value == 0) {
                    _uiState.subscriptionCount.first { it > 0 }
                }
                val qibla = _uiState.value.qiblaBearing
                window.addLast(reading.heading)
                if (window.size > 12) window.removeFirst()
                val steady = window.size >= 8 && angularSpread(window) <= 6f
                _uiState.update {
                    it.copy(
                        deviceHeading   = reading.heading,
                        rotationToQibla = (qibla - reading.heading + 360f) % 360f,
                        compassTrusted  = reading.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM,
                        readingSteady   = steady,
                    )
                }
                }
        }
    }
}
