package app.rafiq.domain.usecase

import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class GetDailyProgressUseCase(
    private val repository: ProgressRepository
) {
    operator fun invoke(date: String): Flow<DailyProgressInfo?> =
        repository.getByDate(date)
}
