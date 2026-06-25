package com.example.dsa_duel.utils

import com.example.dsa_duel.models.BotProfile
import kotlin.random.Random

/**
 * FILE: utils/BotGenerator.kt
 *
 * Generates realistic bot opponents for each duel.
 *
 * NO Android imports. NO Room. NO Hilt.
 * Pure Kotlin — independently testable.
 *
 * WHAT MAKES A GOOD BOT?
 * A bot needs to feel like a real human opponent.
 * Three things make it feel real:
 *
 *   1. FAIR ELO     → bot's rating is close to player's rating
 *                     (within ±100 so matches feel competitive)
 *
 *   2. REALISTIC TIME → stronger bots answer FASTER
 *                       weaker bots answer SLOWER
 *                       with natural randomness (not always the same)
 *
 *   3. REALISTIC ACCURACY → stronger bots answer CORRECTLY more often
 *                           weaker bots make mistakes more often
 *                           again with natural variance
 *
 * BOT PERSONALITY SYSTEM:
 * Each bot has a "personality" that adds flavor beyond just stats.
 * Personalities slightly adjust solve time and accuracy,
 * making each opponent feel distinctly different.
 *
 *   AGGRESSIVE → answers very fast but less accurate
 *   CAREFUL    → answers slower but more accurate
 *   BALANCED   → standard stats, no adjustment
 *   RUSHER     → extremely fast, very inaccurate
 *   ANALYST    → very slow, very accurate
 */
object BotGenerator {

    // ── BOT NAME POOLS ────────────────────────────────────────────

    /**
     * Bot names grouped by rank tier.
     * Higher rank bots get more intimidating names —
     * this is a subtle UX touch that builds tension on the VS screen.
     */
    private val bronzeNames = listOf(
        "byte_learner", "code_newbie", "loop_rookie",
        "stack_zero",   "null_init",  "arr_starter",
        "debug_noob",   "ptr_rookie", "basic_bit"
    )

    private val silverNames = listOf(
        "algo_shadow",  "byte_ghost",  "null_ptr_",
        "stack_runner", "heap_walker", "tree_scout",
        "bit_flipper",  "mod_player",  "recursion_kid"
    )

    private val goldNames = listOf(
        "code_samurai",  "byte_warrior",  "algo_knight",
        "dp_hunter",     "graph_seeker",  "sort_master",
        "binary_phantom","hash_ranger",   "queue_slayer"
    )

    private val platinumNames = listOf(
        "void_pointer",  "stack_overflow_", "algo_spectre",
        "complexity_x",  "dp_phantom",      "bit_sovereign",
        "null_terminator","heap_dominator", "graph_warlord"
    )

    private val diamondNames = listOf(
        "algo_master",   "code_overlord",  "dp_legend",
        "byte_titan",    "null_crusher",   "tle_slayer",
        "o1_god",        "log_n_phantom",  "leetcode_max"
    )

    private val grandmasterNames = listOf(
        "THE_ALGORITHM", "GOD_OF_DSA",   "BINARY_OVERLORD",
        "NP_COMPLETE",   "TURING_GHOST", "O_OF_1",
        "KNUTH_SHADOW",  "DIJKSTRA_X",   "COMPLEXITY_ZERO"
    )

    // ── BOT PERSONALITY TYPES ─────────────────────────────────────

    private enum class BotPersonality(
        val timeMultiplier: Float,    // < 1.0 = faster, > 1.0 = slower
        val accuracyBonus: Float      // added to base accuracy
    ) {
        BALANCED  (timeMultiplier = 1.00f, accuracyBonus =  0.00f),
        AGGRESSIVE(timeMultiplier = 0.75f, accuracyBonus = -0.08f),
        CAREFUL   (timeMultiplier = 1.30f, accuracyBonus = +0.08f),
        RUSHER    (timeMultiplier = 0.55f, accuracyBonus = -0.15f),
        ANALYST   (timeMultiplier = 1.50f, accuracyBonus = +0.12f),
    }

    // ── MAIN FUNCTION ─────────────────────────────────────────────

    /**
     * Generates a bot opponent for a player with [playerElo].
     *
     * The bot is designed to feel like a fair, real opponent:
     *   - ELO within ±100 of player
     *   - Solve time derived from ELO with randomness
     *   - Accuracy derived from ELO with randomness
     *   - Random personality adds further variance
     *   - Name matches rank tier
     *
     * Called by: DuelRepository.prepareDuel()
     *
     * @param playerElo  The player's current ELO rating
     * @return BotProfile ready to display on VS screen
     */
    fun generateBot(playerElo: Int): BotProfile {

        // ── Step 1: Generate bot ELO within ±100 ─────────────────
        // Random.nextInt(-100, 101) gives range -100 to +100
        // coerceIn prevents ELO going below 800 or above 2800
        val eloOffset = Random.nextInt(-100, 101)
        val botElo    = (playerElo + eloOffset).coerceIn(800, 2800)

        // ── Step 2: Pick random personality ──────────────────────
        val personality = BotPersonality.entries.random()

        // ── Step 3: Calculate base solve time from ELO ────────────
        // Higher ELO = faster solve time (lower seconds)
        // Base ranges (in seconds):
        //   ELO 800-1100  → 90-180s (slow)
        //   ELO 1100-1400 → 60-120s (medium)
        //   ELO 1400-1700 → 40-90s  (fast)
        //   ELO 1700-2100 → 25-60s  (very fast)
        //   ELO 2100+     → 15-35s  (extremely fast)
        val baseSolveTime = calculateBaseSolveTime(botElo)

        // Apply personality time multiplier + random variance
        val variance       = Random.nextFloat() * 20f - 10f  // ±10 seconds
        val adjustedTime   = (baseSolveTime * personality.timeMultiplier + variance)
            .toInt()
            .coerceIn(10, 200)  // never < 10s or > 200s

        // ── Step 4: Calculate accuracy from ELO ──────────────────
        // Higher ELO = higher accuracy
        // Base ranges:
        //   ELO 800-1100  → 40-65% accurate
        //   ELO 1100-1400 → 60-80% accurate
        //   ELO 1400-1700 → 72-88% accurate
        //   ELO 1700-2100 → 82-95% accurate
        //   ELO 2100+     → 90-99% accurate
        val baseAccuracy = calculateBaseAccuracy(botElo)

        // Apply personality accuracy bonus + small random variance
        val accuracyVariance = Random.nextFloat() * 0.06f - 0.03f  // ±3%
        val adjustedAccuracy = (baseAccuracy + personality.accuracyBonus + accuracyVariance)
            .coerceIn(0.30f, 0.99f)  // never < 30% or > 99%

        // ── Step 5: Pick name matching rank tier ──────────────────
        val name     = pickBotName(botElo)

        // ── Step 6: Get rank details from EloEngine ───────────────
        val rankName  = EloEngine.getRankName(botElo)
        val rankEmoji = EloEngine.getRankEmoji(botElo)

        return BotProfile(
            name             = name,
            eloRating        = botElo,
            solveTimeSeconds = adjustedTime,
            accuracy         = adjustedAccuracy,
            rankName         = rankName,
            rankEmoji        = rankEmoji
        )
    }

    // ── HELPER: BASE SOLVE TIME ───────────────────────────────────

    /**
     * Calculates base solve time in seconds from bot ELO.
     * Uses linear interpolation within each ELO bracket.
     *
     * Lower ELO = slower (higher seconds)
     * Higher ELO = faster (lower seconds)
     */
    private fun calculateBaseSolveTime(botElo: Int): Float {
        return when (botElo) {
            in 800..1099  -> {
                // Slow tier: 90s to 180s
                // As ELO increases from 800 to 1099, time decreases 180→90
                val progress = (botElo - 800) / 299f
                180f - (progress * 90f)
            }
            in 1100..1399 -> {
                // Medium tier: 60s to 120s
                val progress = (botElo - 1100) / 299f
                120f - (progress * 60f)
            }
            in 1400..1699 -> {
                // Fast tier: 40s to 90s
                val progress = (botElo - 1400) / 299f
                90f - (progress * 50f)
            }
            in 1700..2099 -> {
                // Very fast tier: 25s to 60s
                val progress = (botElo - 1700) / 399f
                60f - (progress * 35f)
            }
            else -> {
                // Grandmaster tier: 15s to 25s
                val progress = (botElo - 2100) / 700f
                25f - (progress * 10f)
            }
        }
    }

    // ── HELPER: BASE ACCURACY ─────────────────────────────────────

    /**
     * Calculates base accuracy (0.0 to 1.0) from bot ELO.
     * Uses linear interpolation within each ELO bracket.
     *
     * Lower ELO = less accurate
     * Higher ELO = more accurate
     */
    private fun calculateBaseAccuracy(botElo: Int): Float {
        return when (botElo) {
            in 800..1099  -> {
                // 40% to 65% accurate
                val progress = (botElo - 800) / 299f
                0.40f + (progress * 0.25f)
            }
            in 1100..1399 -> {
                // 60% to 80% accurate
                val progress = (botElo - 1100) / 299f
                0.60f + (progress * 0.20f)
            }
            in 1400..1699 -> {
                // 72% to 88% accurate
                val progress = (botElo - 1400) / 299f
                0.72f + (progress * 0.16f)
            }
            in 1700..2099 -> {
                // 82% to 95% accurate
                val progress = (botElo - 1700) / 399f
                0.82f + (progress * 0.13f)
            }
            else -> {
                // 90% to 99% accurate
                val progress = (botElo - 2100) / 700f
                0.90f + (progress * 0.09f)
            }
        }
    }

    // ── HELPER: BOT NAME ──────────────────────────────────────────

    /**
     * Picks a random bot name matching the bot's ELO rank tier.
     * Grandmaster bots get all-caps intimidating names.
     * Bronze bots get humble beginner-sounding names.
     */
    private fun pickBotName(botElo: Int): String {
        val pool = when (botElo) {
            in 800..999   -> bronzeNames
            in 1000..1199 -> silverNames
            in 1200..1499 -> goldNames
            in 1500..1799 -> platinumNames
            in 1800..2099 -> diamondNames
            else          -> grandmasterNames
        }
        return pool.random()
    }

    // ── UTILITY: DID BOT ANSWER CORRECTLY? ───────────────────────

    /**
     * Determines if the bot answered correctly for a given duel.
     * Called in DuelViewModel when bot's solve time expires.
     *
     * Uses the bot's accuracy as probability:
     *   accuracy = 0.85 → 85% chance of returning true
     *   accuracy = 0.40 → 40% chance of returning true
     *
     * Random.nextFloat() returns 0.0 to 1.0.
     * If random value < accuracy → bot answered correctly.
     *
     * Example:
     *   accuracy = 0.85, random = 0.72 → 0.72 < 0.85 → TRUE (correct)
     *   accuracy = 0.85, random = 0.91 → 0.91 > 0.85 → FALSE (wrong)
     */
    fun didBotAnswerCorrectly(accuracy: Float): Boolean {
        return Random.nextFloat() < accuracy
    }
}

/*
 * ── BOT GENERATION EXAMPLE ────────────────────────────────────────
 *
 * Player ELO = 1500
 *
 * Step 1: Bot ELO
 *   eloOffset = Random(-100, 100) = +67
 *   botElo    = 1500 + 67 = 1567
 *
 * Step 2: Personality
 *   random pick → CAREFUL
 *   timeMultiplier = 1.30, accuracyBonus = +0.08
 *
 * Step 3: Solve time
 *   botElo 1567 → FAST tier (1400-1699)
 *   progress  = (1567 - 1400) / 299 = 0.559
 *   base time = 90 - (0.559 × 50) = 90 - 27.9 = 62.1s
 *   variance  = Random(-10, 10) = +4
 *   adjusted  = (62.1 × 1.30) + 4 = 80.7 + 4 = 84.7 → 84s
 *
 * Step 4: Accuracy
 *   botElo 1567 → FAST tier (1400-1699)
 *   progress = (1567 - 1400) / 299 = 0.559
 *   base     = 0.72 + (0.559 × 0.16) = 0.72 + 0.089 = 0.809
 *   variance = Random(-0.03, 0.03) = +0.01
 *   adjusted = 0.809 + 0.08 (CAREFUL) + 0.01 = 0.899
 *   → 89.9% accuracy
 *
 * Step 5: Name
 *   botElo 1567 → platinum pool
 *   random pick → "void_pointer"
 *
 * Result: BotProfile(
 *   name             = "void_pointer",
 *   eloRating        = 1567,
 *   solveTimeSeconds = 84,     ← answers when timer hits (120 - 84) = 36s left
 *   accuracy         = 0.899,  ← 90% chance of being correct
 *   rankName         = "PLATINUM",
 *   rankEmoji        = "⚡"
 * )
 *
 * On the duel screen:
 *   Timer starts at 120s
 *   When timer hits 36s → bot "answers"
 *   DuelViewModel calls didBotAnswerCorrectly(0.899)
 *   90% chance bot was right → if right, player must have
 *   already answered correctly to win, else player loses
 *
 * ── WHY LINEAR INTERPOLATION? ────────────────────────────────────
 *
 * Instead of: if (elo > 1400) time = 60
 * We use:     time = max - (progress × range)
 *
 * This creates smooth, continuous transitions between ELO brackets.
 * A player at ELO 1399 gets a bot with solve time ~120s.
 * A player at ELO 1400 gets a bot with solve time ~90s.
 *
 * Without interpolation that jump feels jarring.
 * With interpolation, every 1 ELO point creates a proportionally
 * different bot — the difficulty curve is perfectly smooth.
 */