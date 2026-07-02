package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.repository.DayCompanionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DayCompanionRepositoryImpl(private val db: RafiqDatabase) : DayCompanionRepository {

    override fun getCompletedStations(date: String): Flow<Set<String>> =
        db.dayStationLogQueries.getByDate(date)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.toSet() }

    override suspend fun completeStation(date: String, station: String) =
        withContext(Dispatchers.IO) {
            db.dayStationLogQueries.complete(date, station, Clock.System.now().toEpochMilliseconds())
        }
}
