package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.PlayerStatsEntity
import com.example.dsa_duel.data.TopicMasteryEntity
import com.example.dsa_duel.repositories.StatsRepository
import com.example.dsa_duel.utils.AdaptiveQuestionSelector
import com.example.dsa_duel.utils.EloEngine
import com.example.dsa_duel.utils.TopicAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isProfileMissing: Boolean = false, // 🔥 New Flag

    val displayName: String = "Warrior",
    val greeting: String = "Good morning",

    val eloRating: Int = 1200,
    val rankName: String = "GOLD",
    val rankEmoji: String = "🥇",
    val rankProgress: Float = 0f,
    val peakElo: Int = 1200,

    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val winRate: Int = 0,
    val totalDuels: Int = 0,

    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val streakDays: List<StreakDay> = emptyList(),

    val topicMastery: List<TopicMasteryEntity> = emptyList(),
    val unlockedTopicCount: Int = 0,
    val totalTopicCount: Int = 9,

    val weakestTopic: String? = null,
    val weakestTopicMastery: Int = 0,

    val errorMessage: String? = null
)

data class StreakDay(
    val label: String,
    val isCompleted: Boolean,
    val isToday: Boolean
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val adaptiveSelector: AdaptiveQuestionSelector
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            combine(
                statsRepository.observePlayerStats(),
                statsRepository.observeTopicMastery()
            ) { stats, topics ->
                buildUiState(stats, topics)
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load data: ${e.message}"
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    private fun buildUiState(
        stats: PlayerStatsEntity?,
        topics: List<TopicMasteryEntity>
    ): HomeUiState {
        // 🔥 If stats is null, it means no profile exists in the DB yet
        if (stats == null) {
            return HomeUiState(isLoading = false, isProfileMissing = true)
        }

        val rankName = EloEngine.getRankName(stats.eloRating)
        val rankEmoji = EloEngine.getRankEmoji(stats.eloRating)
        val rankProgress = EloEngine.rankProgress(stats.eloRating)

        val unlockedTopics = topics.filter { it.isUnlocked }
        val weakestTopic = unlockedTopics
            .filter { it.questionsAttempted > 0 }
            .minByOrNull { it.masteryScore }

        val streakDays = buildStreakDays(
            currentStreak = stats.currentStreak,
            lastPlayedMs = stats.lastPlayedDate
        )

        return HomeUiState(
            isLoading = false,
            isProfileMissing = false,
            displayName = stats.displayName.split(" ").first(),
            greeting = getGreeting(),
            eloRating = stats.eloRating,
            rankName = rankName,
            rankEmoji = rankEmoji,
            rankProgress = rankProgress,
            peakElo = stats.peakElo,
            totalWins = stats.totalWins,
            totalLosses = stats.totalLosses,
            winRate = stats.winRate,
            totalDuels = stats.totalDuels,
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak,
            streakDays = streakDays,
            topicMastery = topics.sortedWith(
                compareByDescending<TopicMasteryEntity> { it.isUnlocked }
                    .thenBy { it.masteryScore }
            ),
            unlockedTopicCount = unlockedTopics.size,
            totalTopicCount = topics.size,
            weakestTopic = weakestTopic?.topic,
            weakestTopicMastery = weakestTopic?.masteryPercent ?: 0
        )
    }

    private fun buildStreakDays(currentStreak: Int, lastPlayedMs: Long): List<StreakDay> {
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val today = Calendar.getInstance()
        val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val todayIndex = when (todayDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        val playedToday = if (lastPlayedMs == 0L) false else {
            val lastPlayedCal = Calendar.getInstance().apply { timeInMillis = lastPlayedMs }
            lastPlayedCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    lastPlayedCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        }

        return dayLabels.mapIndexed { index, label ->
            val daysAgo = todayIndex - index
            val isCompleted = when {
                daysAgo < 0 -> false
                daysAgo == 0 -> playedToday
                else -> daysAgo <= (if (playedToday) currentStreak - 1 else currentStreak)
            }
            StreakDay(label, isCompleted, index == todayIndex)
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            in 17..20 -> "Good evening,"
            else -> "Hey,"
        }
    }

    fun initializeProfile(displayName: String, email: String) {
        viewModelScope.launch {
            statsRepository.initializeProfile(displayName, email)
        }
    }

    fun loadTopicAnalysis(onResult: (List<TopicAnalysis>) -> Unit) {
        viewModelScope.launch {
            onResult(adaptiveSelector.getTopicAnalysis())
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
