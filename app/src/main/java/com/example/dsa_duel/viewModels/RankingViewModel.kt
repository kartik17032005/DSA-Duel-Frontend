package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.repositories.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val elo: Int,
    val isPlayer: Boolean = false
)

data class RankingUiState(
    val playerElo: Int = 1200,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRankingData()
    }

    private fun loadRankingData() {
        viewModelScope.launch {
            val currentElo = statsRepository.getCurrentElo()
            val simulatedLeaderboard = generateSimulatedLeaderboard(currentElo)
            
            _uiState.value = RankingUiState(
                playerElo = currentElo,
                leaderboard = simulatedLeaderboard,
                isLoading = false
            )
        }
    }

    private fun generateSimulatedLeaderboard(playerElo: Int): List<LeaderboardEntry> {
        val bots = listOf(
            "BinaryBeast" to 2850,
            "AlgoMaster" to 2600,
            "CodeNinja" to 2450,
            "StackOverflow" to 2300,
            "RecursionRebel" to 2150,
            "PointerPrince" to 1900,
            "ArrayAce" to 1750,
            "GraphGhost" to 1600,
            "TreeTitan" to 1450,
            "SortingSultan" to 1300
        ).map { (name, elo) -> LeaderboardEntry(0, name, elo) }

        val playerEntry = LeaderboardEntry(0, "YOU", playerElo, true)
        
        return (bots + playerEntry)
            .sortedByDescending { it.elo }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }
}
