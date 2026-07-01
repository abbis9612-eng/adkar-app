package app.rafiq.domain.repository

import app.rafiq.domain.model.Khatira
import kotlinx.coroutines.flow.Flow

interface KhatiraRepository {
    fun getKhatira(dayOfYear: Int): Flow<Khatira?>
}
