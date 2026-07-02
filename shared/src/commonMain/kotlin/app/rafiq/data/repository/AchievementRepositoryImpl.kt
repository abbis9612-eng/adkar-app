package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.repository.AchievementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class AchievementRepositoryImpl(private val db: RafiqDatabase) : AchievementRepository {

    override fun getUnlockedKeys(): Flow<Set<String>> =
        db.achievementQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.key }.toSet() }

    override suspend fun unlock(key: String) = withContext(Dispatchers.IO) {
        db.achievementQueries.unlock(key, Clock.System.now().toEpochMilliseconds())
    }
}
