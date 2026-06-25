package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.TopicMasteryDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════
//  DATA MODELS
// ═══════════════════════════════════════════════════════════════

data class EloPoint(
    val index: Int,
    val elo: Int,
    val won: Boolean
)

data class TopicStat(
    val topic: String,
    val wins: Int,
    val total: Int,
    val mastery: Float
) {
    val winRate: Float get() = if (total == 0) 0f else wins.toFloat() / total
}

data class RecentDuel(
    val won: Boolean,
    val topic: String,
    val eloChange: Int,
    val timeTakenSeconds: Int
)

data class AnalyticsUiState(
    val isLoading: Boolean             = false,
    val eloHistory: List<EloPoint>     = emptyList(),
    val topicStats: List<TopicStat>    = emptyList(),
    val recentDuels: List<RecentDuel>  = emptyList(),
    val totalDuels: Int                = 0,
    val totalWins: Int                 = 0,
    val currentElo: Int                = 0,
    val peakElo: Int                   = 0,
    val eloChange7d: Int               = 0,
    val avgAccuracy: Float             = 0f,
    val avgResponseSec: Float          = 0f,
    val bestTopic: String              = "",
    val worstTopic: String             = ""
)

// ═══════════════════════════════════════════════════════════════
//  VIEW MODEL
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val statsDao: PlayerStatsDao,
    private val topicDao: TopicMasteryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // ── 1. Load Local Data (Room) ──────────────────────────────
                val stats = statsDao.getStats()
                val topics = topicDao.getUnlockedTopics()
                val topicStats = topics
                    .map { t ->
                        TopicStat(
                            topic   = t.topic,
                            wins    = t.questionsCorrect,
                            total   = t.questionsAttempted.coerceAtLeast(0),
                            mastery = t.masteryScore
                        )
                    }
                    .sortedByDescending { it.mastery }

                // ── 2. Load Remote Data (Firestore) ────────────────────────
                var eloHistory = emptyList<EloPoint>()
                var recentDuels = emptyList<RecentDuel>()

                // Wrap Firebase access in a sub-try to prevent crash if Firebase isn't initialized
                try {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val historySnap = firestore
                            .collection("users").document(uid)
                            .collection("duel_history")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .limit(20)
                            .get()
                            .await()

                        eloHistory = historySnap.documents.mapIndexedNotNull { i, doc ->
                            val elo = doc.getLong("eloAfter")?.toInt() ?: return@mapIndexedNotNull null
                            val won = doc.getBoolean("won") ?: false
                            EloPoint(index = i + 1, elo = elo, won = won)
                        }

                        recentDuels = historySnap.documents.takeLast(5).mapNotNull { doc ->
                            RecentDuel(
                                won               = doc.getBoolean("won") ?: false,
                                topic             = doc.getString("topic") ?: "Unknown",
                                eloChange         = doc.getLong("eloChange")?.toInt() ?: 0,
                                timeTakenSeconds  = doc.getLong("timeTaken")?.toInt() ?: 0
                            )
                        }.reversed()
                    }
                } catch (e: Exception) {
                    // Firebase failure is non-fatal, we continue with Room data
                }

                // ── 3. Derived Analytics ───────────────────────────────────
                val eloChange7d = if (eloHistory.size >= 2) {
                    eloHistory.last().elo - eloHistory[maxOf(0, eloHistory.size - 8)].elo
                } else 0

                val avgResponseSec = if (recentDuels.isNotEmpty())
                    recentDuels.map { it.timeTakenSeconds }.average().toFloat() else 0f

                val avgAccuracy = if (topicStats.isNotEmpty())
                    topicStats.map { it.winRate }.average().toFloat() else 0f

                val bestTopic  = topicStats.maxByOrNull { it.mastery }?.topic ?: ""
                val worstTopic = topicStats.filter { it.total > 0 }
                    .minByOrNull { it.mastery }?.topic ?: ""

                _uiState.value = AnalyticsUiState(
                    isLoading       = false,
                    eloHistory      = eloHistory,
                    topicStats      = topicStats,
                    recentDuels     = recentDuels,
                    totalDuels      = stats?.totalDuels ?: 0,
                    totalWins       = stats?.totalWins  ?: 0,
                    currentElo      = stats?.eloRating  ?: 0,
                    peakElo         = stats?.peakElo    ?: 0,
                    eloChange7d     = eloChange7d,
                    avgAccuracy     = avgAccuracy,
                    avgResponseSec  = avgResponseSec,
                    bestTopic       = bestTopic,
                    worstTopic      = worstTopic
                )

            } catch (t: Throwable) {
                // Fallback to local data only
                try {
                    val stats = statsDao.getStats()
                    val topics = topicDao.getUnlockedTopics()
                    val topicStats = topics.map { t ->
                        TopicStat(t.topic, t.questionsCorrect, t.questionsAttempted, t.masteryScore)
                    }
                    _uiState.value = AnalyticsUiState(
                        isLoading  = false,
                        topicStats = topicStats,
                        totalDuels = stats?.totalDuels ?: 0,
                        totalWins  = stats?.totalWins  ?: 0,
                        currentElo = stats?.eloRating  ?: 0,
                        peakElo    = stats?.peakElo    ?: 0
                    )
                } catch (inner: Throwable) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun refresh() = load()
}
