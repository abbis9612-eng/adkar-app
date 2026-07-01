package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.PrayerEntry
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.PrayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class PrayerRepositoryImpl(private val db: RafiqDatabase) : PrayerRepository {

    override fun getByDate(date: String): Flow<List<PrayerEntry>> =
        db.prayerLogQueries.getByDate(date)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { p -> p.toDomain() } }

    override suspend fun logPrayer(date: String, prayerName: String, prayed: Boolean) =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            db.transaction {
                db.prayerLogQueries.insertNew(
                    date        = date,
                    prayer_name = prayerName,
                    prayed      = if (prayed) 1L else 0L,
                    marked_at   = now,
                    in_masjid   = 0L,
                    sunnah_done = 0L
                )
                db.prayerLogQueries.updatePrayed(
                    prayed      = if (prayed) 1L else 0L,
                    marked_at   = now,
                    date        = date,
                    prayer_name = prayerName
                )
            }
        }

    override suspend fun updateSunnah(date: String, prayerName: String, done: Boolean) =
        withContext(Dispatchers.IO) {
            db.prayerLogQueries.updateSunnah(
                sunnah_done = if (done) 1L else 0L,
                date        = date,
                prayer_name = prayerName
            )
        }

    override suspend fun getPrayedCount(start: String, end: String): Long =
        withContext(Dispatchers.IO) {
            db.prayerLogQueries.getPrayedCountInRange(start, end).executeAsOne()
        }
}
