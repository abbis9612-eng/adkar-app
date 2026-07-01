package app.rafiq.domain.usecase

import app.rafiq.domain.model.Khatira
import app.rafiq.domain.repository.KhatiraRepository
import kotlinx.coroutines.flow.Flow

class GetKhatiraUseCase(
    private val repository: KhatiraRepository
) {
    operator fun invoke(dayOfYear: Int): Flow<Khatira?> =
        repository.getKhatira(dayOfYear)
}
