package app.rafiqaldhikr.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.platform.JsonResourceReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class City(
    val ar:      String,
    val en:      String,
    val country: String,
    @SerialName("country_ar") val countryAr: String,
    val lat:     Double,
    val lng:     Double,
) {
    /**
     * مطابقة البحث. تتجاهل التشكيل وهمزات الوصل والقطع، لأن من يكتب
     * «الاسكندرية» يقصد «الإسكندرية»، ومن يكتب «makkah» يقصد «Mecca».
     */
    fun matches(query: String): Boolean {
        val q = normalize(query)
        return q in normalize(ar) || q in normalize(en) || q in normalize(countryAr)
    }

    private fun normalize(s: String): String = s
        .lowercase()
        .replace(Regex("[\\u064B-\\u0652\\u0670]"), "")   // التشكيل
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ة', 'ه')
        .replace("ال", "")                                // أل التعريف
        .filter { !it.isWhitespace() }
}

class CityListViewModel(
    private val reader: JsonResourceReader,
) : ViewModel() {

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    init {
        viewModelScope.launch {
            _cities.value = runCatching {
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<List<City>>(reader.readAsset("cities.json"))
            }.getOrDefault(emptyList())
        }
    }
}
