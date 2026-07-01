package app.rafiq.domain.usecase

class CalculateQiblaUseCase {
    private val meccaLat = 21.4225
    private val meccaLng = 39.8262

    operator fun invoke(lat: Double, lng: Double): Float {
        val lat1 = kotlin.math.PI / 180.0 * lat
        val lat2 = kotlin.math.PI / 180.0 * meccaLat
        val dLng = kotlin.math.PI / 180.0 * (meccaLng - lng)
        val y    = kotlin.math.sin(dLng) * kotlin.math.cos(lat2)
        val x    = kotlin.math.cos(lat1) * kotlin.math.sin(lat2) -
                   kotlin.math.sin(lat1) * kotlin.math.cos(lat2) * kotlin.math.cos(dLng)
        return ((kotlin.math.atan2(y, x) * 180.0 / kotlin.math.PI + 360) % 360).toFloat()
    }
}
