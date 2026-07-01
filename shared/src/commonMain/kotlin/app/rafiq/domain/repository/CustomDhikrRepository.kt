package app.rafiq.domain.repository

import app.rafiq.db.CustomDhikr
import kotlinx.coroutines.flow.Flow

interface CustomDhikrRepository {
    fun getAll(): Flow<List<CustomDhikr>>
    suspend fun insert(dhikrText: String, target: Long)
    suspend fun delete(id: Long)
}
