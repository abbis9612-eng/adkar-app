package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.repository.TasbeehRepository
import app.rafiq.domain.repository.TasbeehSessionInfo
import app.rafiq.domain.repository.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class TasbeehRepositoryImpl(private val db: RafiqDatabase) : TasbeehRepository {

    override suspend fun saveSession(
        dhikrText: String, count: Int, target: Int,
        completed: Boolean, durationSeconds: Long, date: String
    ) = withContext(Dispatchers.IO) {
        db.tasbeehSessionQueries.insert(
            dhikr_text       = dhikrText,
            count            = count.toLong(),
            target           = target.toLong(),
            completed        = if (completed) 1L else 0L,
            duration_seconds = durationSeconds,
            date             = date,
            created_at       = Clock.System.now().toEpochMilliseconds()
        )
    }

    override fun getByDate(date: String): Flow<List<TasbeehSessionInfo>> =
        db.tasbeehSessionQueries.getByDate(date)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { s -> s.toDomain() } }

    override fun getTotalCountByDate(date: String): Flow<Long> =
        db.tasbeehSessionQueries.getTotalCountByDate(date)
            .asFlow()
            .mapToOne(Dispatchers.IO)
}
