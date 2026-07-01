package app.rafiq.domain.repository

import app.rafiq.domain.model.PrayerEntry
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    fun getByDate(date: String): Flow<List<PrayerEntry>>
    suspend fun logPrayer(date: String, prayerName: String, prayed: Boolean)
    suspend fun updateSunnah(date: String, prayerName: String, done: Boolean)
    suspend fun getPrayedCount(start: String, end: String): Long
}
