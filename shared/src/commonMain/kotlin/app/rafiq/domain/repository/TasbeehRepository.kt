package app.rafiq.domain.repository

import kotlinx.coroutines.flow.Flow

interface TasbeehRepository {
    suspend fun saveSession(
        dhikrText:       String,
        count:           Int,
        target:          Int,
        completed:       Boolean,
        durationSeconds: Long,
        date:            String
    )
    fun getByDate(date: String): Flow<List<TasbeehSessionInfo>>
    fun getTotalCountByDate(date: String): Flow<Long>
}

data class TasbeehSessionInfo(
    val id:              Long,
    val dhikrText:       String,
    val count:           Int,
    val target:          Int,
    val completed:       Boolean,
    val durationSeconds: Long,
    val date:            String,
    val createdAt:       Long
)

fun app.rafiq.db.TasbeehSession.toDomain() = TasbeehSessionInfo(
    id              = id,
    dhikrText       = dhikr_text,
    count           = count.toInt(),
    target          = target.toInt(),
    completed       = completed == 1L,
    durationSeconds = duration_seconds,
    date            = date,
    createdAt       = created_at
)
