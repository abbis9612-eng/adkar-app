package app.rafiq.domain.repository

import app.rafiq.domain.model.StreakInfo
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    fun getStreak(): Flow<StreakInfo>
    suspend fun recordActivity(date: String)
}
