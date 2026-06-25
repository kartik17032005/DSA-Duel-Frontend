package com.example.dsa_duel.repositories

import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.PlayerStatsEntity
import com.example.dsa_duel.data.TopicMasteryDao
import com.example.dsa_duel.data.DefaultTopics
import com.example.dsa_duel.utils.EloEngine
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * FILE: repositories/StatsRepository.kt
 *
 * Business logic layer for all player statistics.
 */
@Singleton
class StatsRepository @Inject constructor(
    private val playerStatsDao: PlayerStatsDao,
    private val topicMasteryDao: TopicMasteryDao
) {

    // ── OBSERVE (for UI) ──────────────────────────────────────────

    fun observePlayerStats(): Flow<PlayerStatsEntity?> {
        return playerStatsDao.observeStats()
    }

    fun observeTopicMastery() = topicMasteryDao.observeAllTopics()

    // ── PROFILE SETUP ─────────────────────────────────────────────

    suspend fun initializeProfile(
        displayName: String,
        email: String
    ) {
        playerStatsDao.createProfile(
            PlayerStatsEntity(
                displayName = displayName.trim().ifBlank { "Warrior" },
                email       = email
            )
        )

        topicMasteryDao.insertDefaultTopics(DefaultTopics.all)
    }

    suspend fun getPlayerStats(): PlayerStatsEntity? {
        return playerStatsDao.getStats()
    }

    suspend fun getCurrentElo(): Int {
        return playerStatsDao.getCurrentElo() ?: 1200
    }

    // ── CORE: POST-DUEL UPDATE ────────────────────────────────────

    suspend fun processDuelResult(
        playerWon: Boolean,
        botElo: Int,
        topic: String,
        wasCorrect: Boolean,
        wasTimeout: Boolean = false
    ): Int {
        val currentStats = playerStatsDao.getStats() ?: PlayerStatsEntity()
        val currentElo = currentStats.eloRating

        val newElo = EloEngine.calculateNewElo(
            playerElo    = currentElo,
            opponentElo  = botElo,
            playerWon    = playerWon
        )
        val eloChange = newElo - currentElo
        val now = System.currentTimeMillis()

        // ── Step 1: Calculate New Streak ──────────────────────────
        val (newStreak, newLongestStreak) = calculateNewStreak(
            currentStreak  = currentStats.currentStreak,
            longestStreak  = currentStats.longestStreak,
            lastPlayedDate = currentStats.lastPlayedDate,
            todayMs        = now
        )

        // ── Step 2: Update player stats row ──────────────────────
        playerStatsDao.updateAfterDuel(
            won              = if (playerWon) 1 else 0,
            newElo           = newElo,
            todayMs          = now,
            wasTimeout       = if (wasTimeout) 1 else 0,
            newStreak        = newStreak,
            newLongestStreak = newLongestStreak
        )

        if (wasCorrect) {
            playerStatsDao.incrementCorrectAnswers()
        }

        // ── Step 3: Update topic mastery for this topic ───────────
        topicMasteryDao.updateMasteryAfterDuel(
            topic      = topic,
            wasCorrect = if (wasCorrect) 1 else 0,
            now        = now
        )

        checkAndUnlockTopics()

        return eloChange
    }

    /**
     * Logic to determine if the streak continues, resets, or stays same.
     * Only increments if it's a NEW calendar day.
     */
    private fun calculateNewStreak(
        currentStreak: Int,
        longestStreak: Int,
        lastPlayedDate: Long,
        todayMs: Long
    ): Pair<Int, Int> {
        if (lastPlayedDate == 0L) return 1 to max(longestStreak, 1)

        val lastPlayed = Calendar.getInstance().apply { timeInMillis = lastPlayedDate }
        val today = Calendar.getInstance().apply { timeInMillis = todayMs }

        // Compare dates (ignoring time)
        val isSameDay = lastPlayed.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                lastPlayed.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            // Already played today, streak doesn't change
            return currentStreak to longestStreak
        }

        // Check if it's the NEXT day
        val tomorrowOfLastPlayed = (lastPlayed.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isNextDay = tomorrowOfLastPlayed.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                tomorrowOfLastPlayed.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        return if (isNextDay) {
            val nextStreak = currentStreak + 1
            nextStreak to max(longestStreak, nextStreak)
        } else {
            // Missed a day or more
            1 to max(longestStreak, 1)
        }
    }

    // ── TOPIC UNLOCK SYSTEM ───────────────────────────────────────

    suspend fun checkAndUnlockTopics() {
        val arrays       = getMastery("Arrays")
        val linkedList   = getMastery("LinkedList")
        val stacks       = getMastery("Stacks")
        val trees        = getMastery("Trees")
        val graphs       = getMastery("Graphs")
        val sorting      = getMastery("Sorting")

        if (arrays >= 0.40f) {
            topicMasteryDao.unlockTopic("LinkedList")
        }

        if (arrays >= 0.40f) {
            topicMasteryDao.unlockTopic("Strings")
        }

        if (linkedList >= 0.40f) {
            topicMasteryDao.unlockTopic("Stacks")
        }

        if (arrays >= 0.50f && linkedList >= 0.50f) {
            topicMasteryDao.unlockTopic("Trees")
        }

        if (trees >= 0.50f) {
            topicMasteryDao.unlockTopic("Graphs")
        }

        if (arrays >= 0.40f && stacks >= 0.40f) {
            topicMasteryDao.unlockTopic("Sorting")
        }

        if (sorting >= 0.50f) {
            topicMasteryDao.unlockTopic("BinarySearch")
        }

        if (trees >= 0.50f && graphs >= 0.50f) {
            topicMasteryDao.unlockTopic("DP")
        }
    }

    private suspend fun getMastery(topic: String): Float {
        return topicMasteryDao.getMasteryScore(topic) ?: 0f
    }

    // ── PROFILE MANAGEMENT ────────────────────────────────────────

    suspend fun updateDisplayName(newName: String) {
        playerStatsDao.updateDisplayName(newName.trim(), System.currentTimeMillis())
    }

    // ── RESET ─────────────────────────────────────────────────────

    suspend fun resetAllProgress(displayName: String, email: String) {
        playerStatsDao.deleteProfile()
        playerStatsDao.createProfile(
            PlayerStatsEntity(
                displayName = displayName,
                email       = email
            )
        )

        topicMasteryDao.resetAllMastery()
    }
}
