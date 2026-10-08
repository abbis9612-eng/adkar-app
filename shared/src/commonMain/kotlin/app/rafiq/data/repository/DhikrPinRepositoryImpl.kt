package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.DhikrPin
import app.rafiq.domain.repository.DhikrPinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DhikrPinRepositoryImpl(private val db: RafiqDatabase) : DhikrPinRepository {

    override fun all(): Flow<List<DhikrPin>> =
        db.dhikrPinQueries.getAll().asFlow().mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override fun one(dhikrId: Long): Flow<DhikrPin?> =
        db.dhikrPinQueries.getOne(dhikrId).asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun reminders(): List<DhikrPin> = withContext(Dispatchers.IO) {
        db.dhikrPinQueries.getReminders().executeAsList().map { it.toDomain() }
    }

    override suspend fun pin(dhikrId: Long, meeqat: String, offsetMinutes: Int) =
        withContext(Dispatchers.IO) {
            db.dhikrPinQueries.upsert(
                dhikr_id = dhikrId,
                meeqat = meeqat,
                //  الإزاحةُ محصورةٌ في ساعتين: ما جاوزها خرج عن معنى
                //  «بعد المغرب» إلى ميقاتٍ آخر، فيُجدوَل به لا بإزاحة.
                offset_m = offsetMinutes.coerceIn(-120, 120).toLong(),
                pinned_at = Clock.System.now().toEpochMilliseconds(),
            )
        }

    override suspend fun unpin(dhikrId: Long) = withContext(Dispatchers.IO) {
        db.dhikrPinQueries.delete(dhikrId)
    }
}

private fun app.rafiq.db.DhikrPin.toDomain() = DhikrPin(
    dhikrId = dhikr_id,
    meeqat = meeqat,
    offsetMinutes = offset_m.toInt(),
    pinnedAt = pinned_at,
)
