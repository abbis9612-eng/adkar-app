package app.rafiq.data.repository

import app.rafiq.domain.model.Wisdom
import app.rafiq.domain.repository.WisdomRepository
import app.rafiq.platform.JsonResourceReader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class WisdomJson(
    val id:     String,
    @SerialName("text_ar")      val textAr: String,
    val author: String,
    val source: String,
    @SerialName("source_grade") val grade:  String,
)

class WisdomRepositoryImpl(
    private val reader: JsonResourceReader,
) : WisdomRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cached: List<Wisdom>? = null

    override suspend fun forDay(epochDay: Long): Wisdom? {
        val all = cached ?: runCatching {
            json.decodeFromString<List<WisdomJson>>(reader.readAsset("wisdom.json"))
                .map { Wisdom(it.id, it.textAr, it.author, it.source, it.grade) }
        }.getOrDefault(emptyList()).also { cached = it }

        if (all.isEmpty()) return null
        // باقٍ موجبٌ دائماً — % في كوتلن يعطي سالباً للأعداد السالبة، وepochDay
        // سالبٌ قبل ١٩٧٠. لا يحدث عملياً، لكن الفهرس السالب يرمي استثناءً.
        val i = ((epochDay % all.size) + all.size) % all.size
        return all[i.toInt()]
    }
}
