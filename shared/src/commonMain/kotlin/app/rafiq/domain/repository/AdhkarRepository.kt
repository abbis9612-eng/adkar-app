package app.rafiq.domain.repository

import app.rafiq.domain.model.Dhikr
import kotlinx.coroutines.flow.Flow

interface AdhkarRepository {
    fun getByCategory(category: String): Flow<List<Dhikr>>
    fun getCategories(): Flow<List<String>>
    suspend fun getById(id: Long): Dhikr?
}
