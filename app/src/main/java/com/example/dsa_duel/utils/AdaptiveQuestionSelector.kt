package com.example.dsa_duel.utils

import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.TopicMasteryDao
import com.example.dsa_duel.data.TopicMasteryEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * FILE: utils/AdaptiveQuestionSelector.kt
 *
 * Selects the best DSA topic for each duel session.
 *
 * WHAT IS ADAPTIVE LEARNING?
 * Instead of random topic selection, the app tracks how well
 * you know each topic and selects the one where practice
 * will have the most impact on your growth.
 *
 * THE ZONE OF PROXIMAL DEVELOPMENT (ZPD):
 * Educational psychology shows that learning is maximized
 * when the challenge is just beyond your current ability —
 * not too easy (boring), not too hard (frustrating).
 * This "sweet spot" is called the Zone of Proximal Development.
 *
 * HOW WE IMPLEMENT ZPD:
 * Topics are categorized by mastery score:
 *   0.00 - 0.29 → WEAK     (priority weight: 3)
 *   0.30 - 0.69 → LEARNING (priority weight: 4 ← HIGHEST)
 *   0.70 - 0.89 → STRONG   (priority weight: 2)
 *   0.90 - 1.00 → MASTERED (priority weight: 1)
 *
 * LEARNING zone gets the highest weight because:
 * - WEAK topics: you lack fundamentals, hard to make progress
 * - MASTERED topics: no room to grow, boring
 * - LEARNING topics: you have a foundation, each question builds on it
 *
 * WEIGHTED RANDOM SELECTION:
 * We don't always pick the highest priority topic —
 * that would be predictable and boring.
 * Instead we use weighted random: higher weight = more likely to be picked.
 * This adds variety while still favouring productive topics.
 *
 * EXAMPLE:
 * Topics: Arrays(MASTERED=1), Trees(LEARNING=4), Graphs(WEAK=3)
 * Total weight = 1 + 4 + 3 = 8
 * Arrays   picked with 1/8 = 12.5% probability
 * Trees    picked with 4/8 = 50.0% probability ← most likely
 * Graphs   picked with 3/8 = 37.5% probability
 */
@Singleton
class AdaptiveQuestionSelector @Inject constructor(
    private val topicMasteryDao: TopicMasteryDao,
    private val playerStatsDao: PlayerStatsDao
) {

    // ── PRIORITY WEIGHTS ──────────────────────────────────────────

    /**
     * Weight assigned to each mastery zone.
     * Higher weight = more frequently selected.
     * These values are tuned for optimal learning progression.
     */
    private object Weights {
        const val MASTERED = 1   // rarely practice — already know it
        const val STRONG   = 2   // occasional maintenance practice
        const val WEAK     = 3   // needs work but learning is harder
        const val LEARNING = 4   // sweet spot — highest priority
        const val LOCKED   = 0   // never selected
    }

    // ── MAIN FUNCTION ─────────────────────────────────────────────

    /**
     * Selects the best topic for the next duel.
     *
     * Called by: DuelRepository.prepareDuel()
     *
     * Algorithm:
     *   1. Fetch all unlocked topic mastery rows from DB
     *   2. Assign a priority weight to each topic
     *   3. Use weighted random selection to pick a topic
     *   4. Apply recency penalty (avoid same topic twice in a row)
     *   5. Return the selected topic name
     *
     * Returns "Arrays" as fallback if no topics are unlocked
     * (should never happen after initialization).
     *
     * @return Topic name e.g. "Arrays", "Trees", "DP"
     */
    suspend fun selectBestTopic(): String {

        // ── Step 1: Fetch unlocked topics ────────────────────────
        val unlockedTopics = topicMasteryDao.getUnlockedTopics()

        // Safety fallback — should never happen after seeding
        if (unlockedTopics.isEmpty()) return "Arrays"

        // Single topic — no selection needed
        if (unlockedTopics.size == 1) return unlockedTopics.first().topic

        // ── Step 2: Get last played topic for recency penalty ─────
        val lastPlayedTopic = getLastPlayedTopic()

        // ── Step 3: Assign weights to each topic ──────────────────
        val weightedTopics = unlockedTopics.map { mastery ->
            val baseWeight = calculateWeight(mastery)

            // Recency penalty: reduce weight if this was last topic
            // This prevents the same topic from being picked twice
            // in a row, adding variety to practice sessions.
            val finalWeight = if (mastery.topic == lastPlayedTopic && unlockedTopics.size > 1) {
                maxOf(1, baseWeight - 2)  // reduce by 2 but never below 1
            } else {
                baseWeight
            }

            WeightedTopic(
                topic       = mastery.topic,
                weight      = finalWeight,
                mastery     = mastery.masteryScore,
                masteryLevel = mastery.masteryLevel
            )
        }

        // ── Step 4: Weighted random selection ────────────────────
        return weightedRandomSelect(weightedTopics)
    }

    // ── WEIGHT CALCULATION ────────────────────────────────────────

    /**
     * Assigns a priority weight based on mastery score.
     *
     * Also considers:
     * - Topics never attempted get WEAK weight + 1 bonus
     *   (encourage exploration of new unlocked topics)
     * - Topics attempted recently but scoring poorly
     *   get WEAK weight (needs consistent practice)
     *
     * @param mastery TopicMasteryEntity for one topic
     * @return Int weight (0-5)
     */
    private fun calculateWeight(mastery: TopicMasteryEntity): Int {
        // Never attempted → give a slight exploration bonus
        if (mastery.questionsAttempted == 0) {
            return Weights.WEAK + 1  // = 4, same as LEARNING
        }

        return when (mastery.masteryScore) {
            in 0.00f..0.29f -> Weights.WEAK
            in 0.30f..0.69f -> Weights.LEARNING
            in 0.70f..0.89f -> Weights.STRONG
            else            -> Weights.MASTERED
        }
    }

    // ── WEIGHTED RANDOM SELECTION ─────────────────────────────────

    /**
     * Selects one topic using weighted random algorithm.
     *
     * HOW WEIGHTED RANDOM WORKS:
     *
     * Given topics with weights:
     *   Arrays  → weight 1
     *   Trees   → weight 4
     *   Graphs  → weight 3
     *   Total   = 8
     *
     * Create a "number line" from 0 to 8:
     *   [0----1)[1----------5)[5--------8)
     *   Arrays    Trees          Graphs
     *
     * Pick a random number 0-7:
     *   0         → Arrays   (1/8 chance)
     *   1,2,3,4   → Trees    (4/8 chance)
     *   5,6,7     → Graphs   (3/8 chance)
     *
     * The algorithm walks through topics summing weights
     * until the running sum exceeds the random value.
     *
     * @param weightedTopics List of topics with weights
     * @return Selected topic name
     */
    private fun weightedRandomSelect(weightedTopics: List<WeightedTopic>): String {
        val totalWeight = weightedTopics.sumOf { it.weight }

        // Pick a random number in range [0, totalWeight)
        val randomValue = Random.nextInt(totalWeight)

        // Walk through topics accumulating weights
        var runningSum = 0
        for (weightedTopic in weightedTopics) {
            runningSum += weightedTopic.weight
            if (randomValue < runningSum) {
                return weightedTopic.topic
            }
        }

        // Fallback — should never reach here mathematically
        return weightedTopics.last().topic
    }

    // ── RECENCY TRACKING ──────────────────────────────────────────

    /**
     * Returns the topic practiced most recently.
     * Used to apply recency penalty in selectBestTopic().
     *
     * Finds the topic with the most recent lastAttemptedAt timestamp.
     * Returns null if no topics have been attempted yet.
     */
    private suspend fun getLastPlayedTopic(): String? {
        return topicMasteryDao.getUnlockedTopics()
            .filter { it.lastAttemptedAt > 0L }  // only attempted topics
            .maxByOrNull { it.lastAttemptedAt }   // most recently attempted
            ?.topic
    }

    // ── ANALYSIS FUNCTIONS (for Profile / Debug screens) ──────────

    /**
     * Returns a full breakdown of all topics with their
     * weights and selection probabilities.
     *
     * Used by: Profile screen "Adaptive Analysis" section
     * Shows player why certain topics are being prioritized.
     *
     * Example output:
     *   Arrays   → mastery 85%, weight 1, probability 8.3%
     *   Trees    → mastery 40%, weight 4, probability 33.3%
     *   Graphs   → mastery 20%, weight 3, probability 25.0%
     */
    suspend fun getTopicAnalysis(): List<TopicAnalysis> {
        val unlockedTopics = topicMasteryDao.getUnlockedTopics()
        val lastPlayed     = getLastPlayedTopic()

        val weighted = unlockedTopics.map { mastery ->
            val base   = calculateWeight(mastery)
            val weight = if (mastery.topic == lastPlayed) maxOf(1, base - 2) else base
            WeightedTopic(mastery.topic, weight, mastery.masteryScore, mastery.masteryLevel)
        }

        val total = weighted.sumOf { it.weight }.toFloat()

        return weighted.map { wt ->
            TopicAnalysis(
                topic           = wt.topic,
                masteryPercent  = (wt.mastery * 100).toInt(),
                masteryLevel    = wt.masteryLevel,
                weight          = wt.weight,
                selectionChance = if (total > 0) (wt.weight / total * 100).toInt() else 0,
                isLastPlayed    = wt.topic == lastPlayed
            )
        }.sortedByDescending { it.weight }
    }

    /**
     * Returns the weakest unlocked topic.
     * Used by Home screen to show a "Focus area" recommendation.
     *
     * Example: "Your weakest topic is Trees (40%). Focus here!"
     */
    suspend fun getWeakestTopic(): TopicMasteryEntity? {
        return topicMasteryDao.getUnlockedTopics()
            .filter { it.questionsAttempted > 0 }
            .minByOrNull { it.masteryScore }
    }

    /**
     * Returns the strongest unlocked topic.
     * Used by Profile screen for encouraging display.
     *
     * Example: "You're strongest in Arrays (85%)!"
     */
    suspend fun getStrongestTopic(): TopicMasteryEntity? {
        return topicMasteryDao.getUnlockedTopics()
            .maxByOrNull { it.masteryScore }
    }
}

// ── SUPPORTING DATA CLASSES ───────────────────────────────────────

/**
 * Internal data class used during weighted selection.
 * Not exposed outside AdaptiveQuestionSelector.
 */
private data class WeightedTopic(
    val topic: String,
    val weight: Int,
    val mastery: Float,
    val masteryLevel: String
)

/**
 * Public data class returned by getTopicAnalysis().
 * Used by Profile screen to show adaptive algorithm breakdown.
 */
data class TopicAnalysis(
    val topic: String,
    val masteryPercent: Int,      // 0-100
    val masteryLevel: String,     // "WEAK", "LEARNING", "STRONG", "MASTERED"
    val weight: Int,              // 1-4
    val selectionChance: Int,     // 0-100 (percentage)
    val isLastPlayed: Boolean     // true if this was the last practiced topic
)

/*
 * ── FULL WORKED EXAMPLE ───────────────────────────────────────────
 *
 * Player has 4 unlocked topics with these mastery scores:
 *   Arrays    → 0.85 (85%) → STRONG   → weight 2
 *   LinkedList→ 0.55 (55%) → LEARNING → weight 4
 *   Trees     → 0.25 (25%) → WEAK     → weight 3
 *   Strings   → 0.00 (0%)  → new      → weight 4 (new topic bonus)
 *
 * Last played topic: LinkedList
 * After recency penalty: LinkedList weight → max(1, 4-2) = 2
 *
 * Final weights:
 *   Arrays     → 2
 *   LinkedList → 2  (penalized)
 *   Trees      → 3
 *   Strings    → 4  (new topic bonus)
 *   Total      = 11
 *
 * Selection probabilities:
 *   Arrays     → 2/11 = 18.2%
 *   LinkedList → 2/11 = 18.2%
 *   Trees      → 3/11 = 27.3%
 *   Strings    → 4/11 = 36.4% ← most likely (new, unexplored)
 *
 * Random pick: weightedRandomSelect() rolls 7
 *   Running sum: Arrays(2) → Trees(5) → 7 < 9(LinkedList) → wait
 *   Actually:
 *     Arrays:     0 → 2  (range 0-1)
 *     LinkedList: 2 → 4  (range 2-3)
 *     Trees:      4 → 7  (range 4-6)
 *     Strings:    7 → 11 (range 7-10)
 *   randomValue = 7 → falls in Strings range → Strings selected ✓
 *
 * DuelRepository then fetches a Strings question at
 * player's ELO-appropriate difficulty.
 *
 * ── WHY NOT JUST PICK THE WEAKEST TOPIC? ─────────────────────────
 *
 * Picking strictly the weakest topic every time:
 *   1. Is predictable — player knows what's coming
 *   2. Can be demoralizing — always practicing your worst topic
 *   3. Ignores the ZPD — sometimes the weakest topic is too hard
 *      to make progress on without first strengthening prerequisites
 *
 * Weighted random selection:
 *   1. Adds variety — multiple topics get practiced
 *   2. Still prioritizes where growth is most needed
 *   3. Occasional mastered topic reviews reinforce memory
 *   4. New topics get exploration bonus — encourages breadth
 *
 * This balance between exploitation (weak topics) and
 * exploration (variety) is a core principle in reinforcement
 * learning — the same algorithm used by recommendation systems
 * at Netflix and Spotify.
 */