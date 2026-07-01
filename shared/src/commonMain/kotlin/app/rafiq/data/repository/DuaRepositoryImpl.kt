package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.DuaItem
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.DuaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DuaRepositoryImpl(private val db: RafiqDatabase) : DuaRepository {

    override fun getByCategory(category: String): Flow<List<DuaItem>> =
        db.duaQueries.getAllByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { d -> d.toDomain() } }

    override fun getCategories(): Flow<List<String>> =
        db.duaQueries.getCategories()
            .asFlow()
            .mapToList(Dispatchers.IO)

    override fun getFavorites(): Flow<List<DuaItem>> =
        db.duaQueries.getFavorites()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { d -> d.toDomain() } }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        withContext(Dispatchers.IO) {
            db.duaQueries.toggleFavorite(
                is_favorite = if (isFavorite) 1L else 0L,
                id          = id
            )
        }

    override fun search(query: String): Flow<List<DuaItem>> =
        db.duaQueries.search(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { d -> d.toDomain() } }
}
