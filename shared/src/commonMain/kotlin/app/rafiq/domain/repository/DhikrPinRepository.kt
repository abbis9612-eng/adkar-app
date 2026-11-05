package app.rafiq.domain.repository

import app.rafiq.domain.model.DhikrPin
import kotlinx.coroutines.flow.Flow

/**
 * تثبيتُ الذكر — مفضّلةٌ وتذكير.
 *
 * وهما وجهان لفعلٍ واحد: «هذا الذكرُ يعنيني». فمن ثبّته بلا ميقاتٍ فهي
 * مفضّلة، ومن اختار له ميقاتاً صارت تذكيراً.
 */
interface DhikrPinRepository {

    fun all(): Flow<List<DhikrPin>>

    fun one(dhikrId: Long): Flow<DhikrPin?>

    /** التذكيراتُ وحدَها — تُجدوَل مع مواقيت اليوم. */
    suspend fun reminders(): List<DhikrPin>

    /** يثبّت أو يُحدّث. و[meeqat] فارغةً = مفضّلةٌ بلا تذكير. */
    suspend fun pin(dhikrId: Long, meeqat: String = "", offsetMinutes: Int = 0)

    suspend fun unpin(dhikrId: Long)
}
