package app.rafiq.domain.repository

import app.rafiq.domain.model.DailyProgressInfo
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun getByDate(date: String): Flow<DailyProgressInfo?>
    fun getRange(start: String, end: String): Flow<List<DailyProgressInfo>>
    suspend fun ensureExists(date: String)
    suspend fun updateMorning(date: String, done: Boolean)
    suspend fun updateEvening(date: String, done: Boolean)
    suspend fun updateQuranPages(date: String, pages: Long)
    suspend fun updateTasbeeh(date: String, count: Long)
    suspend fun updatePrayers(date: String, count: Long)
}
