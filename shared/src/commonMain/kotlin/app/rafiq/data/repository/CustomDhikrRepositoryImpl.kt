package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.CustomDhikr
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.repository.CustomDhikrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CustomDhikrRepositoryImpl(private val db: RafiqDatabase) : CustomDhikrRepository {

    override fun getAll(): Flow<List<CustomDhikr>> {
        return db.customDhikrQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun insert(dhikrText: String, target: Long) {
        withContext(Dispatchers.IO) {
            db.customDhikrQueries.insert(
                dhikr_text = dhikrText,
                target = target,
                created_at = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    override suspend fun delete(id: Long) {
        withContext(Dispatchers.IO) {
            db.customDhikrQueries.deleteById(id)
        }
    }
}
