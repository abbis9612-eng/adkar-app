package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.Dhikr
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.AdhkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AdhkarRepositoryImpl(private val db: RafiqDatabase) : AdhkarRepository {

    override fun getByCategory(category: String): Flow<List<Dhikr>> =
        db.adhkarQueries.getAllByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getCategories(): Flow<List<String>> =
        db.adhkarQueries.getCategories()
            .asFlow()
            .mapToList(Dispatchers.IO)

    override suspend fun getById(id: Long): Dhikr? =
        withContext(Dispatchers.IO) {
            db.adhkarQueries.getById(id).executeAsOneOrNull()?.toDomain()
        }
}
