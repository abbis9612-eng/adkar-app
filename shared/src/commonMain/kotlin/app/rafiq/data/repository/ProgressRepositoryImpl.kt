package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(private val db: RafiqDatabase) : ProgressRepository {

    override fun getByDate(date: String): Flow<DailyProgressInfo?> =
        db.dailyProgressQueries.getByDate(date)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getRange(start: String, end: String): Flow<List<DailyProgressInfo>> =
        db.dailyProgressQueries.getRange(start, end)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { p -> p.toDomain() } }

    override suspend fun ensureExists(date: String) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.insertNew(date)
        }

    override suspend fun updateMorning(date: String, done: Boolean) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.updateMorning(if (done) 1L else 0L, date)
        }

    override suspend fun updateEvening(date: String, done: Boolean) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.updateEvening(if (done) 1L else 0L, date)
        }

    override suspend fun updateQuranPages(date: String, pages: Long) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.updateQuranPages(pages, date)
        }

    override suspend fun updateTasbeeh(date: String, count: Long) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.updateTasbeeh(count, date)
        }

    override suspend fun updatePrayers(date: String, count: Long) =
        withContext(Dispatchers.IO) {
            db.dailyProgressQueries.updatePrayers(count, date)
        }
}
