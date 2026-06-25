package com.example.dsa_duel.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * FILE: data/TopicMasteryDao.kt
 *
 * DAO for all topic mastery operations.
 *
 * This file has three groups of operations:
 *
 *   GROUP 1 → SETUP    : Insert default topics on first launch
 *   GROUP 2 → READ     : Fetch mastery data for adaptive algorithm + UI
 *   GROUP 3 → UPDATE   : Update mastery after each duel answer
 *
 * The adaptive algorithm reads from this DAO every time a duel
 * starts. The duel result writer updates this DAO every time
 * a duel ends. So this file is touched TWICE per duel session.
 */
@Dao
interface TopicMasteryDao {

    // ── GROUP 1: SETUP ────────────────────────────────────────────

    /**
     * Seeds all 9 default topic rows on first app launch.
     * Called from AppDatabase seeder — runs ONCE ever.
     *
     * OnConflictStrategy.IGNORE = if topics already exist
     * (app restarted), skip insertion. Never wipe existing mastery.
     *
     * The DefaultTopics.all list (defined in TopicMasteryEntity.kt)
     * contains all 9 topics with mastery = 0, only Arrays unlocked.
     *
     * NOTE: Returning List<Long> instead of Unit to avoid KSP "unexpected jvm signature V" bug in Room 2.6.1.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultTopics(topics: List<TopicMasteryEntity>): List<Long>

    // ── GROUP 2: READ ─────────────────────────────────────────────

    /**
     * Returns ALL topic mastery rows as a live Flow.
     * Emits new list every time any topic row changes.
     *
     * Used by:
     *   - HomeViewModel  → powers Topic Mastery grid on Home screen
     *   - RoadmapViewModel → powers the topic dependency tree screen
     *
     * Ordered by masteryScore DESC so weakest topics
     * appear first — immediately shows player what to work on.
     */
    @Query("""
        SELECT * FROM topic_mastery
        ORDER BY isUnlocked DESC, masteryScore ASC
    """)
    fun observeAllTopics(): Flow<List<TopicMasteryEntity>>

    /**
     * Returns all topics as a one-time read (not Flow).
     * Used by AdaptiveQuestionSelector which needs a snapshot
     * of mastery scores at the moment the duel starts.
     *
     * Returns only UNLOCKED topics — locked topics are
     * never selected for duels.
     */
    @Query("""
        SELECT * FROM topic_mastery
        WHERE isUnlocked = 1
        ORDER BY masteryScore ASC
    """)
    suspend fun getUnlockedTopics(): List<TopicMasteryEntity>

    /**
     * Returns mastery for one specific topic.
     * Used after a duel ends to update only the relevant topic row.
     *
     * Example: player just did an Arrays duel →
     *   getMasteryForTopic("Arrays") → fetch current Arrays row
     *   → calculate new mastery
     *   → save updated row back
     */
    @Query("SELECT * FROM topic_mastery WHERE topic = :topic")
    suspend fun getMasteryForTopic(topic: String): TopicMasteryEntity?

    /**
     * Returns mastery score (0.0 to 1.0) for a topic — lightweight.
     * Used by the unlock checker which only needs the score number,
     * not the full entity object.
     *
     * Returns null if topic row doesn't exist (shouldn't happen
     * after seeding but defensive null check is good practice).
     */
    @Query("SELECT masteryScore FROM topic_mastery WHERE topic = :topic")
    suspend fun getMasteryScore(topic: String): Float?

    /**
     * Returns count of unlocked topics.
     * Used by Profile screen to show "X / 9 topics unlocked".
     */
    @Query("SELECT COUNT(*) FROM topic_mastery WHERE isUnlocked = 1")
    suspend fun getUnlockedTopicCount(): Int

    /**
     * Returns all topics that are still locked.
     * Used by Roadmap screen to show which topics to unlock next
     * and what mastery is required to unlock them.
     */
    @Query("SELECT * FROM topic_mastery WHERE isUnlocked = 0")
    fun observeLockedTopics(): Flow<List<TopicMasteryEntity>>

    // ── GROUP 3: UPDATE ───────────────────────────────────────────

    /**
     * THE CORE UPDATE — called after every duel answer.
     *
     * NOTE: Returning Int instead of Unit to avoid KSP "unexpected jvm signature V" bug in Room 2.6.1.
     * NOTE: Removed default value for 'now' parameter as Room DAOs don't support default parameters in KSP.
     */
    @Query("""
        UPDATE topic_mastery SET
            questionsAttempted = questionsAttempted + 1,
            questionsCorrect = questionsCorrect + :wasCorrect,
            masteryScore = CAST(questionsCorrect + :wasCorrect AS FLOAT)
                         / CAST(questionsAttempted + 1 AS FLOAT),
            currentCorrectStreak = CASE
                WHEN :wasCorrect = 1 THEN currentCorrectStreak + 1
                ELSE 0
            END,
            lastAttemptedAt = :now,
            firstAttemptedAt = CASE
                WHEN firstAttemptedAt = 0 THEN :now
                ELSE firstAttemptedAt
            END
        WHERE topic = :topic
    """)
    suspend fun updateMasteryAfterDuel(
        topic: String,
        wasCorrect: Int,   // Int not Boolean — SQLite stores 0/1
        now: Long
    ): Int

    /**
     * Unlocks a topic — sets isUnlocked = 1.
     */
    @Query("""
        UPDATE topic_mastery SET
            isUnlocked = 1
        WHERE topic = :topic
    """)
    suspend fun unlockTopic(topic: String): Int

    /**
     * Unlocks multiple topics at once.
     */
    @Query("""
        UPDATE topic_mastery SET
            isUnlocked = 1
        WHERE topic IN (:topics)
    """)
    suspend fun unlockTopics(topics: List<String>): Int

    /**
     * Full row replacement.
     *
     * NOTE: Returning Int instead of Unit to avoid KSP "unexpected jvm signature V" bug in Room 2.6.1.
     */
    @Update
    suspend fun updateTopicMastery(topic: TopicMasteryEntity): Int

    // ── RESET ─────────────────────────────────────────────────────

    /**
     * Resets ALL topic mastery to zero.
     */
    @Query("""
        UPDATE topic_mastery SET
            masteryScore = 0,
            questionsAttempted = 0,
            questionsCorrect = 0,
            currentCorrectStreak = 0,
            firstAttemptedAt = 0,
            lastAttemptedAt = 0,
            isUnlocked = CASE WHEN topic = 'Arrays' THEN 1 ELSE 0 END
    """)
    suspend fun resetAllMastery(): Int
}

/*
 * ── HOW updateMasteryAfterDuel WORKS — STEP BY STEP ──────────────
 *
 * Before duel (Arrays row):
 *   questionsAttempted = 10
 *   questionsCorrect   = 7
 *   masteryScore       = 0.70
 *
 * Player answers Arrays question CORRECTLY (wasCorrect = 1):
 *   questionsAttempted = 10 + 1          = 11
 *   questionsCorrect   = 7  + 1          = 8
 *   masteryScore       = 8.0 / 11.0      = 0.727
 *   currentCorrectStreak = 0 + 1         = 1 (or +1 if was already on streak)
 *
 * Player answers Arrays question INCORRECTLY (wasCorrect = 0):
 *   questionsAttempted = 10 + 1          = 11
 *   questionsCorrect   = 7  + 0          = 7  (unchanged)
 *   masteryScore       = 7.0 / 11.0      = 0.636  ← score DROPS
 *   currentCorrectStreak = 0             (reset to 0)
 *
 * This means mastery scores are self-correcting — a topic you were
 * good at but then start getting wrong will gradually drop in score,
 * causing the adaptive algorithm to prioritize it for practice again.
 *
 * ── WHY Int INSTEAD OF Boolean FOR wasCorrect ────────────────────
 *
 * SQLite doesn't have a native Boolean type.
 * It stores true/false as 1/0 integers.
 * Room can handle Boolean → Int conversion automatically,
 * but using Int directly in this query makes the SQL arithmetic
 * work cleanly:
 *
 *   questionsCorrect + :wasCorrect
 *   → 7 + 1 = 8  (correct)
 *   → 7 + 0 = 7  (incorrect)
 *
 * If we used Boolean, we'd need CASE WHEN which is more verbose.
 * Int keeps the SQL compact and readable.
 *
 * In DuelViewModel, convert like this before calling:
 *   topicMasteryDao.updateMasteryAfterDuel(
 *       topic      = "Arrays",
 *       wasCorrect = if (playerAnsweredCorrectly) 1 else 0,
 *       now        = System.currentTimeMillis()
 *   )
 *
 * ── UNLOCK CHECKER LOGIC (lives in repository) ───────────────────
 *
 * After every mastery update, the repository checks:
 *
 *   Arrays >= 0.40  → unlock LinkedList, Strings
 *   LinkedList >= 0.40  → unlock Stacks
 *   Arrays >= 0.50 AND LinkedList >= 0.50  → unlock Trees
 *   Trees >= 0.50  → unlock Graphs
 *   Arrays >= 0.40 AND Stacks >= 0.40  → unlock Sorting
 *   Sorting >= 0.50  → unlock BinarySearch
 *   Trees >= 0.50 AND Graphs >= 0.50  → unlock DP
 *
 * If any condition is newly met → call unlockTopic(topicName)
 * The Flow in observeAllTopics() automatically pushes the update
 * to the UI — no manual refresh needed.
 */
