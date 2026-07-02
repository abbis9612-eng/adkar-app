package app.rafiqaldhikr.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.model.StreakInfo
import app.rafiq.domain.repository.AchievementRepository
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class ProfileViewModel(
    private val progressRepo:    ProgressRepository,
    private val streakRepo:      StreakRepository,
    private val prefsRepo:       PrefsRepository,
    private val achievementRepo: AchievementRepository
) : ViewModel() {

    // الإنجازات المفتوحة سابقاً — تبقى مفتوحة للأبد
    val unlockedAchievements: StateFlow<Set<String>> = achievementRepo.getUnlockedKeys()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun persistUnlocked(keys: List<String>) {
        viewModelScope.launch {
            keys.forEach { achievementRepo.unlock(it) }
        }
    }

    data class UiState(
        val streak:        StreakInfo               = StreakInfo(0L, 0L, ""),
        val todayProgress: DailyProgressInfo?       = null,
        val weekProgress:  List<DailyProgressInfo>  = emptyList(),
        val isLoading:     Boolean                  = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val now     = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today   = now.date.toString()
            val weekAgo = now.date.minus(6, DateTimeUnit.DAY).toString()

            combine(
                streakRepo.getStreak(),
                progressRepo.getByDate(today),
                progressRepo.getRange(weekAgo, today)
            ) { streak, todayP, weekP ->
                UiState(
                    streak        = streak,
                    todayProgress = todayP,
                    weekProgress  = weekP,
                    isLoading     = false
                )
            }.collect { _uiState.value = it }
        }
    }
}
