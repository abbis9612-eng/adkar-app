package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.StreakInfo
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.StreakRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StreakRepositoryImpl(private val db: RafiqDatabase) : StreakRepository {

    override fun getStreak(): Flow<StreakInfo> =
        db.streakDataQueries.get()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row ->
                row?.toDomain() ?: StreakInfo(0L, 0L, "")
            }

    override suspend fun recordActivity(date: String) = withContext(Dispatchers.IO) {
        db.streakDataQueries.insertHistory(date)
    }
}
