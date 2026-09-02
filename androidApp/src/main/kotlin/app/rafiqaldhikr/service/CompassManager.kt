package app.rafiqaldhikr.service

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * قراءةُ البوصلة ودقّتُها معاً.
 *
 * كان `onAccuracyChanged` فارغاً — تُهمَل الدقّةُ التي يعلنها النظام، فتُعرض
 * القبلةُ بثقةٍ واحدةٍ سواءٌ كان المستشعر مضبوطاً أم مشوَّشاً بمعدنٍ قريب.
 * وشاشةُ قبلةٍ تجزم بلا تحفّظٍ أسوأُ من شاشةٍ تعتذر: المصلّي يستقبل ما تقول.
 */
class CompassManager(private val context: Context) {

    private val sensorManager  = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val isAvailable: Boolean get() = rotationSensor != null

    /**
     * @param heading الاتّجاه بالدرجات من **الشمال الحقيقي** · [accuracy] من
     *   ثوابت `SensorManager.SENSOR_STATUS_*`: 3 عالية · 2 متوسطة · 1 منخفضة
     *   · 0 غير موثوقة · ‎−1 لم يُبلَّغ بعد.
     */
    data class Reading(val heading: Float, val accuracy: Int)

    /**
     * قراءةُ البوصلة مصحَّحةً بانحراف المكان المغناطيسي.
     *
     * **الخطأ الذي كان:** `getOrientation` يعطي زاويةً من الشمال
     * **المغناطيسي**، واتّجاهُ القبلة في [app.rafiq.domain.usecase.CalculateQiblaUseCase]
     * محسوبٌ من الشمال **الحقيقي**. فكان الفرقُ بينهما — وهو انحرافُ المكان
     * — يدخل في الإبرة كلَّها: أربعُ درجاتٍ إلى ستٍّ في العراق والشام،
     * وعشرٌ إلى عشرين في أمريكا الشمالية وأستراليا. والإبرةُ تجزم وهي
     * منحرفة.
     *
     * و[GeomagneticField] يحسب الانحرافَ محلياً من نموذج WMM المشحون في
     * أندرويد — بلا إنترنت، فقاعدةُ «يعمل دون اتّصال» سليمة.
     *
     * @param lat خط عرض المستخدم · [lng] خط طوله. بلا إحداثيات لا تصحيح
     *   (والشاشةُ أصلاً لا تعرض اتّجاهاً بلا موقع).
     */
    fun getReadingFlow(lat: Double? = null, lng: Double? = null): Flow<Reading> = callbackFlow {
        if (rotationSensor == null) {
            close(IllegalStateException("Rotation sensor not available"))
            return@callbackFlow
        }

        val declination: Float =
            if (lat != null && lng != null) {
                GeomagneticField(
                    lat.toFloat(), lng.toFloat(), 0f, System.currentTimeMillis()
                ).declination
            } else 0f

        var acc = -1
        val listener = object : SensorEventListener {
            private val rotMatrix   = FloatArray(9)
            private val remapped    = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)

                /*  إعادةُ ربط المحاور بدوران الشاشة.
                 *
                 *  قُفلُ الاتّجاه العموديّ أُزيل من الـmanifest، فصارت الشاشة
                 *  تدور. و`getOrientation` يقيس من محاور الجهاز لا من محاور
                 *  العرض — فبلا هذا تنحرف الإبرةُ تسعين درجةً كاملة عند
                 *  إمالة الهاتف.  */
                val m = when (displayRotation()) {
                    Surface.ROTATION_90 -> {
                        SensorManager.remapCoordinateSystem(
                            rotMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remapped
                        ); remapped
                    }
                    Surface.ROTATION_180 -> {
                        SensorManager.remapCoordinateSystem(
                            rotMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remapped
                        ); remapped
                    }
                    Surface.ROTATION_270 -> {
                        SensorManager.remapCoordinateSystem(
                            rotMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, remapped
                        ); remapped
                    }
                    else -> rotMatrix
                }

                SensorManager.getOrientation(m, orientation)
                val magnetic = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val trueNorth = magnetic + declination
                trySend(Reading((trueNorth % 360f + 360f) % 360f, acc))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                acc = accuracy
            }
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.rotation ?: Surface.ROTATION_0
        } else {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.rotation
        }
}
