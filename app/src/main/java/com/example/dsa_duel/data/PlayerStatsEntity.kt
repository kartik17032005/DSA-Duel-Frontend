package com.example.dsa_duel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FILE: data/PlayerStatsEntity.kt
 *
 * Stores the player's profile and competitive stats.
 *
 * KEY DESIGN DECISION:
 * This table always has exactly ONE row — the current player.
 * We hardcode id = 1 as the primary key for that single row.
 *
 * Why one row?
 * DSA Duel Lite is a single-player app (vs bots).
 * There's only one human player on this device.
 * So we don't need multiple rows — just update the same row
 * after every duel.
 *
 * Analogy: Like a "save slot" in a single-player video game.
 * There's only one save file, and we keep overwriting it
 * with the player's latest stats.
 */
@Entity(tableName = "player_stats")
data class PlayerStatsEntity(

    // ── Primary Key ───────────────────────────────────────────────
    // Always 1. Never changes. This is how we always find
    // the one-and-only player row.
    @PrimaryKey
    val id: Int = 1,

    // ── Identity ──────────────────────────────────────────────────

    // Player's display name — pulled from Firebase Auth on first login
    // and saved here so the Home screen can show it offline.
    // Example: "Kartik"
    val displayName: String = "Warrior",

    // Player's email — stored for display on Profile screen
    val email: String = "",

    // ── ELO Rating ────────────────────────────────────────────────

    // The player's current ELO rating.
    // All players start at 1200 — this is the universal starting point
    // used by Chess.com, Codeforces, and most competitive systems.
    //
    // ELO ranges in DSA Duel:
    //   800  - 1000  → BRONZE
    //   1000 - 1200  → SILVER
    //   1200 - 1500  → GOLD
    //   1500 - 1800  → PLATINUM
    //   1800 - 2100  → DIAMOND
    //   2100+        → GRANDMASTER
    val eloRating: Int = 1200,

    // The highest ELO the player has ever reached.
    // Shown on profile as "Peak ELO" — never decreases even if
    // the player loses matches after reaching it.
    val peakElo: Int = 1200,

    // ── Match History ─────────────────────────────────────────────

    // Total duels won against bots
    val totalWins: Int = 0,

    // Total duels lost against bots
    val totalLosses: Int = 0,

    // Total duels where player ran out of time (timeout)
    val totalTimeouts: Int = 0,

    // ── Streak Tracking ───────────────────────────────────────────

    // How many consecutive DAYS the player has played at least one duel.
    // Resets to 0 if they skip a day.
    // This is what drives the flame streak card on the Home screen.
    val currentStreak: Int = 0,

    // The longest streak the player has ever achieved.
    // Never decreases. Shown on Profile screen.
    val longestStreak: Int = 0,

    // Epoch milliseconds of the last day the player completed a duel.
    // Used to calculate whether the streak should continue or reset.
    //
    // Logic:
    //   Today - lastPlayedDate < 48 hours → streak continues
    //   Today - lastPlayedDate >= 48 hours → streak resets to 0
    //
    // We use 48h (not 24h) to be forgiving — if someone plays at
    // 11pm one day and 1am the next, that's still a valid streak.
    val lastPlayedDate: Long = 0L,

    // ── Session Stats ─────────────────────────────────────────────

    // Total number of questions the player has ever answered
    // (correct + incorrect combined)
    val totalQuestionsAnswered: Int = 0,

    // Total number of questions answered correctly across all duels
    val totalCorrectAnswers: Int = 0,

    // ── Timestamps ───────────────────────────────────────────────

    // When the player profile was first created (epoch ms)
    // Used to show "Member since X" on the Profile screen
    val createdAt: Long = System.currentTimeMillis(),

    // When the player last updated their profile (epoch ms)
    val updatedAt: Long = System.currentTimeMillis()
) {
    // ── Computed properties (not stored in DB) ────────────────────
    // These are calculated ON THE FLY from stored values.
    // Room ignores these — they're just helper getters.

    /**
     * Win rate as a percentage (0-100).
     * Example: 34 wins, 12 losses → 73%
     * Returns 0 if no games played yet.
     */
    val winRate: Int
        get() {
            val total = totalWins + totalLosses
            return if (total == 0) 0
            else ((totalWins.toFloat() / total) * 100).toInt()
        }

    /**
     * Total duels played (wins + losses + timeouts)
     */
    val totalDuels: Int
        get() = totalWins + totalLosses + totalTimeouts

    /**
     * Returns the rank name based on current ELO.
     * Used by Home screen ELO card and Profile screen.
     */
    val rankName: String
        get() = when (eloRating) {
            in 0..999    -> "BRONZE"
            in 1000..1199 -> "SILVER"
            in 1200..1499 -> "GOLD"
            in 1500..1799 -> "PLATINUM"
            in 1800..2099 -> "DIAMOND"
            else          -> "GRANDMASTER"
        }

    /**
     * Returns the rank emoji for visual display.
     * Used next to rank name in the ELO card.
     */
    val rankEmoji: String
        get() = when (eloRating) {
            in 0..999    -> "🥉"
            in 1000..1199 -> "🥈"
            in 1200..1499 -> "🥇"
            in 1500..1799 -> "⚡"
            in 1800..2099 -> "💎"
            else          -> "👑"
        }

    /**
     * How far the player is (0.0 to 1.0) through their current rank.
     * Used to fill the ELO progress bar on the Home screen.
     *
     * Example: ELO 1350 in GOLD (1200-1500 range)
     *   progress = (1350 - 1200) / (1500 - 1200) = 150/300 = 0.50
     */
    val rankProgress: Float
        get() = when (eloRating) {
            in 0..999    -> (eloRating - 0)    / 1000f
            in 1000..1199 -> (eloRating - 1000) / 200f
            in 1200..1499 -> (eloRating - 1200) / 300f
            in 1500..1799 -> (eloRating - 1500) / 300f
            in 1800..2099 -> (eloRating - 1800) / 300f
            else          -> 1f
        }.coerceIn(0f, 1f)
}

/*
 * ── HOW PLAYER STATS ARE UPDATED ─────────────────────────────────
 *
 * After every duel, PlayerStatsDao.updateAfterDuel() is called.
 * It does NOT replace the whole row — it only updates specific columns.
 * This is more efficient than deleting and re-inserting the row.
 *
 * Update flow after a duel:
 *
 * Player wins:
 *   eloRating    += EloEngine.eloGain(...)
 *   totalWins    += 1
 *   currentStreak = check date → increment or reset
 *   peakElo      = max(peakElo, newElo)
 *   lastPlayedDate = today
 *
 * Player loses:
 *   eloRating    += EloEngine.eloLoss(...) ← this is negative
 *   totalLosses  += 1
 *   currentStreak = check date → increment or reset
 *   lastPlayedDate = today
 *
 * ── EXAMPLE ROW IN DATABASE ──────────────────────────────────────
 *
 * id            = 1
 * displayName   = "Kartik"
 * email         = "k@gmail.com"
 * eloRating     = 1847
 * peakElo       = 1902
 * totalWins     = 34
 * totalLosses   = 12
 * totalTimeouts = 3
 * currentStreak = 7
 * longestStreak = 12
 * lastPlayedDate = 1714000000000  (epoch ms)
 * winRate       = 73  (computed, not stored)
 * rankName      = "DIAMOND"  (computed, not stored)
 */