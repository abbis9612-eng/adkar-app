package app.rafiqaldhikr.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CompassManager(private val context: Context) {

    private val sensorManager  = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val isAvailable: Boolean get() = rotationSensor != null

    fun getHeadingFlow(): Flow<Float> = callbackFlow {
        if (rotationSensor == null) {
            close(IllegalStateException("Rotation sensor not available"))
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            private val rotMatrix   = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                SensorManager.getOrientation(rotMatrix, orientation)
                val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend((degrees + 360f) % 360f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
