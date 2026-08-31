package app.rafiqaldhikr.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
     * @param heading الاتّجاه بالدرجات من الشمال · [accuracy] من ثوابت
     *   `SensorManager.SENSOR_STATUS_*`: 3 عالية · 2 متوسطة · 1 منخفضة
     *   · 0 غير موثوقة · ‎−1 لم يُبلَّغ بعد.
     */
    data class Reading(val heading: Float, val accuracy: Int)

    fun getReadingFlow(): Flow<Reading> = callbackFlow {
        if (rotationSensor == null) {
            close(IllegalStateException("Rotation sensor not available"))
            return@callbackFlow
        }
        var acc = -1
        val listener = object : SensorEventListener {
            private val rotMatrix   = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                SensorManager.getOrientation(rotMatrix, orientation)
                val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend(Reading((degrees + 360f) % 360f, acc))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                acc = accuracy
            }
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
