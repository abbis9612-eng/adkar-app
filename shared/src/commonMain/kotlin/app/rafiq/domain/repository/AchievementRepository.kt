package app.rafiq.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * الإنجازات المفتوحة تبقى مفتوحة للأبد — تُخزن لحظة فتحها.
 */
interface AchievementRepository {
    fun getUnlockedKeys(): Flow<Set<String>>
    suspend fun unlock(key: String)
}
