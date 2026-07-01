package app.rafiq.domain.usecase

import app.rafiq.db.RafiqDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class UpdateStreakUseCase(
    private val db: RafiqDatabase
) {
    suspend operator fun invoke(today: String) = withContext(Dispatchers.IO) {
        db.transaction {
            db.streakDataQueries.insertHistory(today)

            val current = db.streakDataQueries.get().executeAsOneOrNull()
            if (current == null) {
                db.streakDataQueries.upsert(
                    current_streak   = 1L,
                    longest_streak   = 1L,
                    last_active_date = today
                )
                return@transaction
            }

            val last = current.last_active_date
            if (last == today) return@transaction

            val newCurrent = if (isConsecutiveDay(last, today))
                current.current_streak + 1L else 1L
            val newLongest = maxOf(newCurrent, current.longest_streak)

            db.streakDataQueries.updateStreak(
                current_streak   = newCurrent,
                longest_streak   = newLongest,
                last_active_date = today
            )
        }
    }

    private fun isConsecutiveDay(last: String, today: String): Boolean {
        if (last.isEmpty()) return false
        return try {
            val lastDate  = LocalDate.parse(last)
            val todayDate = LocalDate.parse(today)
            todayDate == lastDate.plus(1, DateTimeUnit.DAY)
        } catch (e: Exception) { false }
    }
}
