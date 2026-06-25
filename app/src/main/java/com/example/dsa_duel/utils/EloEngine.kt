package com.example.dsa_duel.utils

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * FILE: utils/EloEngine.kt
 *
 * Pure Kotlin implementation of the ELO rating system.
 *
 * NO Android imports. NO Room. NO Hilt. NO coroutines.
 * Just math — completely isolated and independently testable.
 *
 * WHAT IS ELO?
 * The ELO system calculates skill ratings for competitive games.
 * Originally designed for chess by Arpad Elo in the 1960s.
 * Now used by: Chess.com, Codeforces, League of Legends,
 *              LeetCode, Lichess, and DSA Duel.
 *
 * CORE PRINCIPLE:
 * Your rating changes based on TWO things:
 *   1. Did you win or lose?
 *   2. Were you EXPECTED to win or lose?
 *
 * Beat a much stronger player → gain a LOT of ELO
 * Beat a much weaker player   → gain a LITTLE ELO
 * Lose to a much stronger player → lose a LITTLE ELO
 * Lose to a much weaker player   → lose a LOT of ELO
 *
 * THE FORMULA (3 lines of math):
 *
 *   Expected = 1 / (1 + 10 ^ ((opponentElo - playerElo) / 400))
 *   Actual   = 1.0 if won, 0.0 if lost
 *   Delta    = K × (Actual - Expected)
 *   NewElo   = OldElo + Delta
 *
 * THE K-FACTOR:
 *   K controls how dramatically ELO changes per match.
 *   Higher K = more volatile (big swings per match)
 *   Lower K  = more stable (small changes per match)
 *
 *   Standard K values:
 *     K = 32 → beginners/new players (used in DSA Duel)
 *     K = 24 → intermediate players
 *     K = 16 → grandmasters (FIDE uses this)
 *
 *   We use K = 32 for all players because:
 *     - DSA Duel players are learning, not grandmasters
 *     - Higher K makes early progress feel rewarding
 *     - Players reach their "true" skill level faster
 */
object EloEngine {

    // ── CONSTANTS ─────────────────────────────────────────────────

    /**
     * K-Factor: controls how much each match changes ELO.
     * K = 32 means a single match can change ELO by at most ±32.
     * In practice, changes are between ±4 and ±28 depending
     * on the ELO difference between players.
     */
    private const val K_FACTOR = 32

    /**
     * The divisor in the expected score formula.
     * Standard ELO uses 400 — this means a player 400 ELO
     * higher is expected to win approximately 90% of the time.
     * Changing this value changes how "steep" the advantage curve is.
     */
    private const val DIVISOR = 400.0

    /**
     * Minimum ELO a player can drop to.
     * Prevents ELO from going below 100 (floor).
     * A real beginner should never be rated below 100.
     */
    private const val MIN_ELO = 100

    /**
     * Maximum ELO a player can reach.
     * Theoretical ceiling of 3000 — practically unreachable
     * but prevents runaway ELO from bugs.
     */
    private const val MAX_ELO = 3000

    // ── CORE FORMULA ──────────────────────────────────────────────

    /**
     * Calculates the EXPECTED score for the player.
     *
     * Expected score = probability of winning, as a decimal.
     * Range: 0.0 (certain loss) to 1.0 (certain win)
     *
     * Formula:
     *   E = 1 / (1 + 10 ^ ((opponentElo - playerElo) / 400))
     *
     * EXAMPLES:
     *   Player 1200 vs Opponent 1200 → E = 0.500 (50/50)
     *   Player 1200 vs Opponent 1600 → E = 0.091 (9% win chance)
     *   Player 1600 vs Opponent 1200 → E = 0.909 (91% win chance)
     *   Player 1200 vs Opponent 1201 → E = 0.499 (barely below 50%)
     *
     * WHY 10 AS THE BASE?
     * Using base 10 gives a clean interpretation:
     * Every 400 ELO difference = 10x more likely to win.
     * Chess uses 10, some systems use e (Euler's number).
     */
    fun expectedScore(playerElo: Int, opponentElo: Int): Double {
        return 1.0 / (1.0 + 10.0.pow((opponentElo - playerElo) / DIVISOR))
    }

    /**
     * Calculates the new ELO after a match.
     *
     * Formula:
     *   newElo = oldElo + K × (actualScore - expectedScore)
     *
     * Where:
     *   actualScore = 1.0 if player won, 0.0 if player lost
     *   expectedScore = result of expectedScore() above
     *   K = K_FACTOR (32)
     *
     * Result is ROUNDED to nearest integer (ELO is always whole number)
     * and CLAMPED between MIN_ELO and MAX_ELO.
     *
     * WORKED EXAMPLES:
     *
     * Example 1: Equal players (both 1500), player wins
     *   expected = 1 / (1 + 10^0) = 0.500
     *   delta    = 32 × (1.0 - 0.500) = +16
     *   newElo   = 1500 + 16 = 1516
     *
     * Example 2: Player (1500) beats stronger bot (1800)
     *   expected = 1 / (1 + 10^(300/400)) = 0.151
     *   delta    = 32 × (1.0 - 0.151) = +27
     *   newElo   = 1500 + 27 = 1527
     *
     * Example 3: Player (1500) loses to weaker bot (1200)
     *   expected = 1 / (1 + 10^(-300/400)) = 0.849
     *   delta    = 32 × (0.0 - 0.849) = -27
     *   newElo   = 1500 - 27 = 1473
     *
     * Example 4: Player (1500) loses to stronger bot (1800)
     *   expected = 1 / (1 + 10^(300/400)) = 0.151
     *   delta    = 32 × (0.0 - 0.151) = -5
     *   newElo   = 1500 - 5 = 1495
     *   (small loss because losing to stronger opponent was expected)
     */
    fun calculateNewElo(
        playerElo: Int,
        opponentElo: Int,
        playerWon: Boolean
    ): Int {
        val expected    = expectedScore(playerElo, opponentElo)
        val actual      = if (playerWon) 1.0 else 0.0
        val delta       = K_FACTOR * (actual - expected)
        val newElo      = playerElo + delta.roundToInt()
        return newElo.coerceIn(MIN_ELO, MAX_ELO)
    }

    /**
     * Returns only the ELO GAIN if player wins.
     * Always a positive number.
     *
     * Used by:
     *   - DuelRepository.prepareDuel() to show "+24" on VS screen
     *   - ResultScreen to show ELO gained after winning
     *
     * Example: eloGain(1500, 1800) → +27
     */
    fun eloGain(playerElo: Int, opponentElo: Int): Int {
        return calculateNewElo(playerElo, opponentElo, playerWon = true) - playerElo
    }

    /**
     * Returns only the ELO LOSS if player loses.
     * Always a negative number.
     *
     * Used by:
     *   - DuelRepository.prepareDuel() to show "-5" on VS screen
     *   - ResultScreen to show ELO lost after losing
     *
     * Example: eloLoss(1500, 1200) → -27
     */
    fun eloLoss(playerElo: Int, opponentElo: Int): Int {
        return calculateNewElo(playerElo, opponentElo, playerWon = false) - playerElo
    }

    /**
     * Returns formatted ELO change string for display.
     * Always includes + or - sign.
     *
     * Examples:
     *   formatEloChange(24)  → "+24"
     *   formatEloChange(-19) → "-19"
     *   formatEloChange(0)   → "+0"  (rare but handled)
     *
     * Used by: ResultScreen ELO change display
     */
    fun formatEloChange(eloChange: Int): String {
        return if (eloChange >= 0) "+$eloChange" else "$eloChange"
    }

    /**
     * Determines rank name from ELO rating.
     * Single source of truth — used everywhere rank needs displaying.
     *
     * Ranks mirror competitive coding platforms:
     *   BRONZE     → 0    - 999
     *   SILVER     → 1000 - 1199
     *   GOLD       → 1200 - 1499
     *   PLATINUM   → 1500 - 1799
     *   DIAMOND    → 1800 - 2099
     *   GRANDMASTER→ 2100+
     */
    fun getRankName(elo: Int): String {
        return when (elo) {
            in 0..999     -> "BRONZE"
            in 1000..1199 -> "SILVER"
            in 1200..1499 -> "GOLD"
            in 1500..1799 -> "PLATINUM"
            in 1800..2099 -> "DIAMOND"
            else          -> "GRANDMASTER"
        }
    }

    /**
     * Returns rank emoji for visual display.
     * Paired with getRankName() for UI rendering.
     */
    fun getRankEmoji(elo: Int): String {
        return when (elo) {
            in 0..999     -> "🥉"
            in 1000..1199 -> "🥈"
            in 1200..1499 -> "🥇"
            in 1500..1799 -> "⚡"
            in 1800..2099 -> "💎"
            else          -> "👑"
        }
    }

    /**
     * Returns progress within current rank (0.0 to 1.0).
     * Used to fill the ELO progress bar on Home screen.
     *
     * Example: ELO 1350 in GOLD (1200–1499 range)
     *   progress = (1350 - 1200) / (1500 - 1200)
     *            = 150 / 300
     *            = 0.50  → bar is half full
     */
    fun rankProgress(elo: Int): Float {
        return when (elo) {
            in 0..999     -> (elo - 0)    / 1000f
            in 1000..1199 -> (elo - 1000) / 200f
            in 1200..1499 -> (elo - 1200) / 300f
            in 1500..1799 -> (elo - 1500) / 300f
            in 1800..2099 -> (elo - 1800) / 300f
            else          -> 1f
        }.coerceIn(0f, 1f)
    }

    /**
     * Simulates a full ELO table for a given player ELO.
     * Returns potential outcomes against different opponent strengths.
     *
     * Used by: Profile screen "ELO breakdown" section
     * Shows player how much they gain/lose against weak/equal/strong bots.
     *
     * Example for player ELO 1500:
     *   vs 1200 (weak)  → win +4,  lose -27
     *   vs 1500 (equal) → win +16, lose -16
     *   vs 1800 (strong)→ win +27, lose -5
     */
    fun getEloTable(playerElo: Int): List<EloTableRow> {
        val offsets = listOf(-300, -150, 0, +150, +300)
        return offsets.map { offset ->
            val opponentElo = (playerElo + offset).coerceIn(MIN_ELO, MAX_ELO)
            EloTableRow(
                opponentElo  = opponentElo,
                opponentRank = getRankName(opponentElo),
                gainIfWin    = eloGain(playerElo, opponentElo),
                lossIfLose   = eloLoss(playerElo, opponentElo)
            )
        }
    }
}

// ── SUPPORTING DATA CLASS ─────────────────────────────────────────

/**
 * One row in the ELO outcome table.
 * Used by getEloTable() for the Profile screen breakdown.
 */
data class EloTableRow(
    val opponentElo: Int,
    val opponentRank: String,
    val gainIfWin: Int,     // positive number
    val lossIfLose: Int     // negative number
)

/*
 * ── UNIT TESTS FOR THIS FILE ──────────────────────────────────────
 *
 * Create: test/java/com/example/dsa_duel/utils/EloEngineTest.kt
 *
 * class EloEngineTest {
 *
 *     @Test
 *     fun `equal players - winner gains 16`() {
 *         val gain = EloEngine.eloGain(1500, 1500)
 *         assertEquals(16, gain)
 *     }
 *
 *     @Test
 *     fun `equal players - loser loses 16`() {
 *         val loss = EloEngine.eloLoss(1500, 1500)
 *         assertEquals(-16, loss)
 *     }
 *
 *     @Test
 *     fun `beating stronger opponent gives more elo`() {
 *         val gainVsStrong = EloEngine.eloGain(1500, 1800)
 *         val gainVsEqual  = EloEngine.eloGain(1500, 1500)
 *         assertTrue(gainVsStrong > gainVsEqual)
 *     }
 *
 *     @Test
 *     fun `losing to stronger opponent costs less elo`() {
 *         val lossVsStrong = EloEngine.eloLoss(1500, 1800)
 *         val lossVsEqual  = EloEngine.eloLoss(1500, 1500)
 *         assertTrue(lossVsStrong > lossVsEqual) // less negative
 *     }
 *
 *     @Test
 *     fun `elo never drops below minimum`() {
 *         val newElo = EloEngine.calculateNewElo(101, 3000, false)
 *         assertTrue(newElo >= 100)
 *     }
 *
 *     @Test
 *     fun `elo never exceeds maximum`() {
 *         val newElo = EloEngine.calculateNewElo(2999, 100, true)
 *         assertTrue(newElo <= 3000)
 *     }
 *
 *     @Test
 *     fun `expected score is 0_5 for equal players`() {
 *         val expected = EloEngine.expectedScore(1500, 1500)
 *         assertEquals(0.5, expected, 0.001)
 *     }
 *
 *     @Test
 *     fun `rank name matches elo range`() {
 *         assertEquals("GOLD",     EloEngine.getRankName(1350))
 *         assertEquals("DIAMOND",  EloEngine.getRankName(1900))
 *         assertEquals("BRONZE",   EloEngine.getRankName(800))
 *     }
 * }
 *
 * Run with: Right-click EloEngineTest → Run
 * These tests run WITHOUT an emulator — pure JVM.
 * This is one of the strongest things you can show in an interview.
 */