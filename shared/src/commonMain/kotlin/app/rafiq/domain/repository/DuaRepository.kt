package app.rafiq.domain.repository

import app.rafiq.domain.model.DuaItem
import kotlinx.coroutines.flow.Flow

interface DuaRepository {
    fun getByCategory(category: String): Flow<List<DuaItem>>
    fun getCategories(): Flow<List<String>>
    fun getFavorites(): Flow<List<DuaItem>>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    fun search(query: String): Flow<List<DuaItem>>
}
