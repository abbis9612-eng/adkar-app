package app.rafiq.data.repository

import app.rafiq.domain.model.City
import app.rafiq.domain.repository.CityRepository
import app.rafiq.platform.JsonResourceReader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class CityJson(
    val ar:      String,
    val en:      String,
    val country: String,
    @SerialName("country_ar") val countryAr: String,
    val lat:     Double,
    val lng:     Double,
)

class CityRepositoryImpl(
    private val reader: JsonResourceReader,
) : CityRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cached: List<City>? = null

    /**
     * القائمة داخل التطبيق فالاختيار يعمل بلا اتصال — وهو الفارق الجوهري
     * عن الترميز العكسي الذي يحتاج شبكة. وقد صار التطبيق بلا إذن إنترنت
     * أصلاً، فهذا هو المسار الوحيد الممكن.
     */
    override suspend fun all(): List<City> = cached ?: runCatching {
        json.decodeFromString<List<CityJson>>(reader.readAsset("cities.json"))
            .map { City(it.ar, it.en, it.country, it.countryAr, it.lat, it.lng) }
    }.getOrDefault(emptyList()).also { cached = it }
}
