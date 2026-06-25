package com.example.dsa_duel.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * FILE: data/PlayerStatsDao.kt
 *
 * DAO for all player profile and stats operations.
 */
@Dao
interface PlayerStatsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createProfile(stats: PlayerStatsEntity): Long

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getStats(): PlayerStatsEntity?

    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun observeStats(): Flow<PlayerStatsEntity?>

    @Query("SELECT eloRating FROM player_stats WHERE id = 1")
    suspend fun getCurrentElo(): Int?

    @Update
    suspend fun updateFullProfile(stats: PlayerStatsEntity): Int

    /**
     * Updates player stats after a duel.
     * Streak logic is now handled in the Repository for better precision.
     */
    @Query("""
        UPDATE player_stats SET
            eloRating = :newElo,
            peakElo = MAX(peakElo, :newElo),
            totalWins = totalWins + CASE WHEN :won = 1 AND :wasTimeout = 0 THEN 1 ELSE 0 END,
            totalLosses = totalLosses + CASE WHEN :won = 0 AND :wasTimeout = 0 THEN 1 ELSE 0 END,
            totalTimeouts = totalTimeouts + CASE WHEN :wasTimeout = 1 THEN 1 ELSE 0 END,
            currentStreak = :newStreak,
            longestStreak = :newLongestStreak,
            lastPlayedDate = :todayMs,
            totalQuestionsAnswered = totalQuestionsAnswered + 1,
            updatedAt = :todayMs
        WHERE id = 1
    """)
    suspend fun updateAfterDuel(
        won: Int,
        newElo: Int,
        todayMs: Long,
        wasTimeout: Int,
        newStreak: Int,
        newLongestStreak: Int
    ): Int

    @Query("""
        UPDATE player_stats SET
            totalCorrectAnswers = totalCorrectAnswers + 1
        WHERE id = 1
    """)
    suspend fun incrementCorrectAnswers(): Int

    @Query("""
        UPDATE player_stats SET
            displayName = :name,
            updatedAt = :now
        WHERE id = 1
    """)
    suspend fun updateDisplayName(name: String, now: Long): Int

    @Query("UPDATE player_stats SET currentStreak = 0 WHERE id = 1")
    suspend fun resetStreak(): Int

    @Query("SELECT COUNT(*) FROM player_stats WHERE id = 1")
    suspend fun profileExists(): Int

    @Query("DELETE FROM player_stats")
    suspend fun deleteProfile(): Int
}
