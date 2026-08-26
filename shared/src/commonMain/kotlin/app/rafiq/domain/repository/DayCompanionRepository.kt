package app.rafiq.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * سجل محطات «رفيق اليوم» — يوم المسلم من الاستيقاظ إلى النوم،
 * مبني على مراسي الصلوات الخمس.
 */
interface DayCompanionRepository {
    fun getCompletedStations(date: String): Flow<Set<String>>

    /** تاريخٌ من [start] إلى [end] شاملين: تاريخ اليوم ← معرّفات محطّاته المتمّة. */
    fun getCompletedRange(start: String, end: String): Flow<Map<String, Set<String>>>
    suspend fun completeStation(date: String, station: String)
}
