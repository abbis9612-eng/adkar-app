package app.rafiq.data.repository

import app.rafiq.db.RafiqDatabase
import app.rafiq.data.remote.RafiqApi
import app.rafiq.domain.model.Khatira
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.KhatiraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KhatiraRepositoryImpl(
    private val db:  RafiqDatabase,
    private val api: RafiqApi
) : KhatiraRepository {

    override fun getKhatira(dayOfYear: Int): Flow<Khatira?> = flow {
        // Offline-first: emit local data first
        val local = db.khatiraQueries.getByDayOfYear(dayOfYear.toLong()).executeAsOneOrNull()
        emit(local?.toDomain())

        // Then try remote update
        val remote = api.getKhatira(dayOfYear).getOrNull()
        if (remote != null) {
            db.khatiraQueries.updateFromRemote(
                text    = remote.text,
                source  = remote.source,
                reflection = remote.reflection,
                version = remote.version,
                day     = dayOfYear.toLong()
            )
            val updated = db.khatiraQueries.getByDayOfYear(dayOfYear.toLong()).executeAsOneOrNull()
            emit(updated?.toDomain())
        }
    }
}
