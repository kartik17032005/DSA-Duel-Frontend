package com.example.dsa_duel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FILE: data/TopicMasteryEntity.kt
 *
 * Tracks the player's skill level PER DSA TOPIC.
 *
 * KEY DESIGN DECISION:
 * One row per topic. So if there are 9 topics, there are 9 rows.
 * Each row tracks how many questions the player attempted and
 * got correct for that specific topic.
 *
 * WHY THIS EXISTS:
 * Without topic mastery, your app would pick questions randomly.
 * With topic mastery, your app picks questions from topics the player
 * is weak at (30%-70% mastery) — this is the adaptive algorithm.
 *
 * Real world analogy:
 * Think of it like a school report card — one grade per subject.
 * The adaptive system sees you're weak in "Trees" (40%) and strong
 * in "Arrays" (85%), so it gives you more Tree questions to practice.
 *
 * TABLE STRUCTURE (9 rows — one per topic):
 * ┌──────────────┬──────────────┬─────────────┬───────────────┬───────────┐
 * │ topic        │ masteryScore │ attempted   │ correct       │ unlocked  │
 * ├──────────────┼──────────────┼─────────────┼───────────────┼───────────┤
 * │ Arrays       │ 0.85         │ 20          │ 17            │ true      │
 * │ LinkedList   │ 0.60         │ 15          │ 9             │ true      │
 * │ Trees        │ 0.40         │ 10          │ 4             │ true      │
 * │ Graphs       │ 0.00         │ 0           │ 0             │ false     │ ← LOCKED
 * │ DP           │ 0.00         │ 0           │ 0             │ false     │ ← LOCKED
 * │ Strings      │ 0.70         │ 10          │ 7             │ true      │
 * │ Stacks       │ 0.50         │ 8           │ 4             │ true      │
 * │ Sorting      │ 0.30         │ 10          │ 3             │ true      │
 * │ BinarySearch │ 0.00         │ 0           │ 0             │ false     │ ← LOCKED
 * └──────────────┴──────────────┴─────────────┴───────────────┴───────────┘
 */
@Entity(tableName = "topic_mastery")
data class TopicMasteryEntity(

    // ── Primary Key ───────────────────────────────────────────────
    // The topic name IS the primary key — unique per topic.
    // Room uses this string to find and update the correct row.
    // Example: "Arrays", "Trees", "DP"
    @PrimaryKey
    val topic: String,

    // ── Mastery Score ─────────────────────────────────────────────
    // A Float between 0.0 and 1.0 representing skill in this topic.
    //
    // How it's calculated:
    //   masteryScore = questionsCorrect / questionsAttempted
    //
    // Examples:
    //   17 correct out of 20 attempted → 0.85 (85%)
    //    4 correct out of 10 attempted → 0.40 (40%)
    //    0 correct out of  0 attempted → 0.00 (new topic)
    //
    // Mastery ranges used by adaptive algorithm:
    //   0.00 - 0.29 → WEAK     → high priority for practice
    //   0.30 - 0.69 → LEARNING → medium priority (sweet spot)
    //   0.70 - 0.89 → STRONG   → low priority
    //   0.90 - 1.00 → MASTERED → very low priority
    val masteryScore: Float = 0f,

    // ── Raw Counters ──────────────────────────────────────────────
    // We store raw counts (not just the percentage) so we can
    // recalculate masteryScore accurately as more data comes in.
    //
    // Why not just store the percentage?
    // Example of why raw counts matter:
    //   After 2 questions: 1 correct → 50%
    //   After 10 questions: 6 correct → 60%
    //   These are very different confidence levels!
    //   With raw counts we can weight reliability of the score.

    // Total questions attempted in this topic (correct + incorrect)
    val questionsAttempted: Int = 0,

    // Total questions answered correctly in this topic
    val questionsCorrect: Int = 0,

    // ── Topic Unlocking System ────────────────────────────────────
    // Not all topics are available from day one.
    // Topics unlock when prerequisite topics reach certain mastery.
    //
    // Unlock dependency tree:
    //   Arrays (unlocked by default — starting topic)
    //     → unlocks LinkedList  (when Arrays >= 40%)
    //     → unlocks Strings     (when Arrays >= 40%)
    //   LinkedList
    //     → unlocks Stacks      (when LinkedList >= 40%)
    //   Arrays + LinkedList
    //     → unlocks Trees       (when both >= 50%)
    //   Trees
    //     → unlocks Graphs      (when Trees >= 50%)
    //   Arrays + Stacks
    //     → unlocks Sorting     (when both >= 40%)
    //   Sorting
    //     → unlocks BinarySearch (when Sorting >= 50%)
    //   Trees + Graphs
    //     → unlocks DP          (when both >= 50%)
    //
    // Locked topics show with a 🔒 on the Roadmap screen.
    val isUnlocked: Boolean = false,

    // ── Streak per topic ──────────────────────────────────────────
    // How many consecutive correct answers in this topic.
    // Used to show small topic-level streaks on mastery cards.
    // Example: "3 in a row ✓" shown on the Arrays mastery card.
    val currentCorrectStreak: Int = 0,

    // ── Timestamps ───────────────────────────────────────────────

    // When the player first attempted a question in this topic (epoch ms)
    // 0L means never attempted
    val firstAttemptedAt: Long = 0L,

    // When the player last attempted a question in this topic (epoch ms)
    // Used to show "Last practiced 2 days ago" on mastery card
    val lastAttemptedAt: Long = 0L
) {

    // ── Computed properties (NOT stored in DB) ────────────────────

    /**
     * Mastery as a percentage (0-100) for display purposes.
     * Example: masteryScore 0.85 → masteryPercent 85
     */
    val masteryPercent: Int
        get() = (masteryScore * 100).toInt()

    /**
     * Human-readable mastery level label.
     * Used on the Topic Mastery cards on Home screen.
     */
    val masteryLevel: String
        get() = when (masteryScore) {
            in 0f..0.29f  -> "WEAK"
            in 0.30f..0.69f -> "LEARNING"
            in 0.70f..0.89f -> "STRONG"
            else            -> "MASTERED"
        }

    /**
     * Color code for mastery level.
     * Returned as hex string — used in UI to color the progress bar.
     */
    val masteryColorHex: String
        get() = when (masteryScore) {
            in 0f..0.29f    -> "#ef4444"  // Red   — weak
            in 0.30f..0.69f -> "#f59e0b"  // Amber — learning
            in 0.70f..0.89f -> "#06b6d4"  // Cyan  — strong
            else            -> "#22c55e"  // Green — mastered
        }

    /**
     * Priority score for the adaptive algorithm.
     * Higher priority = more likely to be selected for next duel.
     *
     * Logic:
     * - WEAK topics (0-30%)   → priority 3 (highest — needs most work)
     * - LEARNING (30-70%)     → priority 4 (sweet spot for growth)
     * - STRONG (70-90%)       → priority 2 (occasionally revisit)
     * - MASTERED (90-100%)    → priority 1 (rarely needed)
     * - LOCKED                → priority 0 (never selected)
     *
     * The sweet spot is LEARNING because:
     * - Too easy (mastered) → boring, no growth
     * - Too hard (locked/new) → frustrating, no confidence
     * - Just right (learning) → challenging but achievable
     * This is called the "Zone of Proximal Development" in education.
     */
    val adaptivePriority: Int
        get() {
            if (!isUnlocked) return 0
            return when (masteryScore) {
                in 0.90f..1.0f  -> 1  // mastered
                in 0f..0.29f    -> 3  // weak — push harder
                in 0.70f..0.89f -> 2  // strong — light maintenance
                else            -> 4  // learning — highest priority
            }
        }

    /**
     * Recalculates masteryScore from raw counters.
     * Returns a new copy with updated masteryScore.
     * Called by TopicMasteryDao after each duel answer.
     */
    fun withUpdatedMastery(wasCorrect: Boolean): TopicMasteryEntity {
        val newAttempted = questionsAttempted + 1
        val newCorrect   = questionsCorrect + (if (wasCorrect) 1 else 0)
        val newStreak    = if (wasCorrect) currentCorrectStreak + 1 else 0
        val newScore     = newCorrect.toFloat() / newAttempted.toFloat()

        return copy(
            questionsAttempted   = newAttempted,
            questionsCorrect     = newCorrect,
            masteryScore         = newScore,
            currentCorrectStreak = newStreak,
            lastAttemptedAt      = System.currentTimeMillis(),
            firstAttemptedAt     = if (firstAttemptedAt == 0L)
                System.currentTimeMillis()
            else firstAttemptedAt
        )
    }
}

/*
 * ── COMPANION: DEFAULT TOPICS ─────────────────────────────────────
 * Used by AppDatabase seeder to initialize the 9 topic rows
 * when the app launches for the first time.
 *
 * Arrays is unlocked by default (isUnlocked = true).
 * All others start locked and unlock as player progresses.
 */
object DefaultTopics {
    val all = listOf(
        TopicMasteryEntity(topic = "Arrays",       isUnlocked = true),
        TopicMasteryEntity(topic = "LinkedList",   isUnlocked = false),
        TopicMasteryEntity(topic = "Strings",      isUnlocked = false),
        TopicMasteryEntity(topic = "Stacks",       isUnlocked = false),
        TopicMasteryEntity(topic = "Trees",        isUnlocked = false),
        TopicMasteryEntity(topic = "Sorting",      isUnlocked = false),
        TopicMasteryEntity(topic = "BinarySearch", isUnlocked = false),
        TopicMasteryEntity(topic = "Graphs",       isUnlocked = false),
        TopicMasteryEntity(topic = "DP",           isUnlocked = false),
    )
}

/*
 * ── HOW THE ADAPTIVE ALGORITHM USES THIS ─────────────────────────
 *
 * Every time a duel is requested:
 *
 * 1. Fetch all TopicMasteryEntity rows from DB
 * 2. Filter to only UNLOCKED topics
 * 3. Sort by adaptivePriority (highest first)
 * 4. Pick the highest priority topic
 *    (with some randomness to avoid always picking the same one)
 * 5. Use that topic + player's ELO to query QuestionDao
 * 6. Return the matched question to DuelViewModel
 *
 * After duel ends:
 * 1. Call withUpdatedMastery(wasCorrect) on the relevant topic row
 * 2. Save updated row back to DB via TopicMasteryDao
 * 3. Check unlock conditions → unlock new topics if thresholds met
 * 4. Update PlayerStatsEntity with new ELO
 */