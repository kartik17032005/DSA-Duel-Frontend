package com.example.dsa_duel.repositories

import com.example.dsa_duel.data.QuestionDao
import com.example.dsa_duel.data.QuestionEntity
import com.example.dsa_duel.data.QuestionSeeder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: repositories/QuestionRepository.kt
 *
 * Handles all question-related business logic.
 *
 * TWO MAIN RESPONSIBILITIES:
 *
 *   1. SEEDING → Insert questions into Room on first launch.
 *                Check count first to avoid re-inserting every launch.
 *
 *   2. FETCHING → Get the right question for each duel.
 *                 Uses a fallback chain so the app never crashes
 *                 even when the perfect match isn't available.
 *
 * FALLBACK CHAIN (most specific → most general):
 *
 *   Level 1: topic + difficulty + ELO range  (perfect match)
 *       ↓ (if null)
 *   Level 2: topic + difficulty              (relax ELO constraint)
 *       ↓ (if null)
 *   Level 3: topic only                      (relax difficulty)
 *       ↓ (if null)
 *   Level 4: any random question             (last resort)
 *       ↓ (if null — should never happen after seeding)
 *   Level 5: throw QuestionNotFoundException
 *
 * This chain ensures the duel ALWAYS starts, even when:
 *   - Player's ELO is in a range with few questions
 *   - A topic has fewer questions than expected
 *   - Questions for a specific difficulty are exhausted
 */
@Singleton
class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) {

    // ── SEEDING ───────────────────────────────────────────────────

    /**
     * Seeds the question bank on first launch.
     *
     * HOW IT WORKS:
     *   1. Check current question count in DB
     *   2. If count > 0 → already seeded → skip (fast path)
     *   3. If count == 0 → first launch → insert all questions
     *
     * Called from:
     *   - Application class onCreate()
     *   - OR DuelRepository init block
     *
     * Safe to call multiple times — the count check prevents
     * duplicate insertions. QuestionDao uses IGNORE conflict
     * strategy as an extra safety net.
     *
     * On a device with 50 questions, this check takes ~1ms.
     * The actual insert (first launch only) takes ~50-100ms
     * on IO thread — never blocks the UI.
     */
    suspend fun seedQuestionsIfNeeded() {
        val existingCount = questionDao.getQuestionCount()
        if (existingCount == 0) {
            // First launch — insert all questions from seeder
            questionDao.insertQuestions(QuestionSeeder.questions)
        }
        // If count > 0, do nothing — already seeded
    }

    /**
     * Forces a re-seed regardless of current count.
     * Used in development/testing only.
     * In production, seedQuestionsIfNeeded() is always preferred.
     */
    suspend fun forceSeed() {
        questionDao.deleteAllQuestions()
        questionDao.insertQuestions(QuestionSeeder.questions)
    }

    // ── FETCHING FOR ADAPTIVE ALGORITHM ──────────────────────────

    /**
     * THE CORE FUNCTION — gets the right question for a duel.
     *
     * Called by DuelRepository.startDuel() after:
     *   1. Player taps "Find Duel"
     *   2. AdaptiveQuestionSelector picks the best topic
     *   3. This function fetches a question for that topic
     *
     * Parameters:
     *   [topic]      → selected by AdaptiveQuestionSelector
     *                  Example: "Arrays", "Trees", "DP"
     *   [difficulty] → derived from player's ELO
     *                  "EASY" / "MEDIUM" / "HARD"
     *   [playerElo]  → used to filter by minElo/maxElo columns
     *
     * Returns QuestionEntity — never null (uses fallback chain).
     * Throws QuestionNotFoundException only if DB is completely empty
     * (which should never happen after seeding).
     */
    suspend fun getQuestionForDuel(
        topic: String,
        difficulty: String,
        playerElo: Int
    ): QuestionEntity {

        // ── Level 1: Perfect match ────────────────────────────────
        // All three criteria must match.
        // This is the ideal path — question perfectly suited for player.
        val perfectMatch = questionDao.getAdaptiveQuestion(
            topic      = topic,
            difficulty = difficulty,
            playerElo  = playerElo
        )
        if (perfectMatch != null) return perfectMatch

        // ── Level 2: Relax ELO constraint ────────────────────────
        // Same topic + difficulty but ignore ELO range.
        // Happens when player's ELO is at the edges (very low or very high).
        // Example: ELO 2200 player — few questions have maxElo >= 2200.
        val relaxedEloMatch = questionDao.getAdaptiveQuestion(
            topic      = topic,
            difficulty = difficulty,
            playerElo  = 1200  // use neutral ELO to widen the range
        )
        if (relaxedEloMatch != null) return relaxedEloMatch

        // ── Level 3: Try adjacent difficulty ─────────────────────
        // Same topic, but one step easier or harder difficulty.
        // Happens when topic has no questions at exact difficulty level.
        val adjacentDifficulty = getAdjacentDifficulty(difficulty)
        for (altDifficulty in adjacentDifficulty) {
            val altMatch = questionDao.getAdaptiveQuestion(
                topic      = topic,
                difficulty = altDifficulty,
                playerElo  = 1200
            )
            if (altMatch != null) return altMatch
        }

        // ── Level 4: Any question from this topic ─────────────────
        // Ignores both difficulty and ELO.
        // Last resort before giving up on the selected topic.
        val topicOnly = questionDao.getRandomQuestionByTopic(topic)
        if (topicOnly != null) return topicOnly

        // ── Level 5: Any question from ANY topic ──────────────────
        // Complete fallback — topic preference ignored.
        // Should NEVER reach here after proper seeding.
        val anyQuestion = questionDao.getAnyRandomQuestion()
        if (anyQuestion != null) return anyQuestion

        // ── Level 6: Throw — DB is empty ─────────────────────────
        // This means seedQuestionsIfNeeded() was never called.
        // Or all questions were manually deleted.
        throw QuestionNotFoundException(
            "No questions found in database. " +
                    "Ensure seedQuestionsIfNeeded() was called on app launch."
        )
    }

    /**
     * Returns adjacent difficulty levels to try as fallback.
     * Order matters — we try the "easier" direction first
     * to avoid overwhelming players, then try harder.
     *
     * EASY   → try [MEDIUM]
     * MEDIUM → try [EASY, HARD]  (try easier first)
     * HARD   → try [MEDIUM]
     */
    private fun getAdjacentDifficulty(difficulty: String): List<String> {
        return when (difficulty) {
            "EASY"   -> listOf("MEDIUM")
            "MEDIUM" -> listOf("EASY", "HARD")
            "HARD"   -> listOf("MEDIUM")
            else     -> listOf("MEDIUM")
        }
    }

    // ── READ FOR UI ───────────────────────────────────────────────

    /**
     * Returns all questions as a live Flow.
     * Used by the question browser screen (future feature).
     */
    fun observeAllQuestions(): Flow<List<QuestionEntity>> {
        return questionDao.getAllQuestions()
    }

    /**
     * Returns all questions for a specific topic as a Flow.
     * Used by TopicDetailScreen to show question list per topic.
     */
    fun observeQuestionsByTopic(topic: String): Flow<List<QuestionEntity>> {
        return questionDao.getAllByTopic(topic)
    }

    /**
     * Returns a specific question by ID.
     * Used by ResultScreen to show question + explanation
     * after a duel ends (in case the state was lost).
     */
    suspend fun getQuestionById(id: Int): QuestionEntity? {
        return questionDao.getById(id)
    }

    /**
     * Returns count of questions per topic.
     * Used by Topic Mastery cards to show "17/20 qs" progress.
     */
    suspend fun getQuestionCountForTopic(topic: String): Int {
        return questionDao.getCountByTopic(topic)
    }

    /**
     * Returns all distinct topic names.
     * Used by the topic chip selector on the Duel setup screen.
     */
    suspend fun getAllTopics(): List<String> {
        return questionDao.getAllTopics()
    }

    /**
     * Returns total question count.
     * Used internally for seed checking and for
     * displaying "X questions available" on Profile screen.
     */
    suspend fun getTotalQuestionCount(): Int {
        return questionDao.getQuestionCount()
    }
}

// ── CUSTOM EXCEPTION ──────────────────────────────────────────────

/**
 * Thrown when no question can be found after exhausting
 * all fallback levels. Should never happen in production
 * after proper seeding.
 *
 * DuelRepository catches this and transitions to DuelState.Error.
 * User sees: "No questions available. Please restart the app."
 */
class QuestionNotFoundException(message: String) : Exception(message)

/*
 * ── FALLBACK CHAIN EXAMPLE ────────────────────────────────────────
 *
 * Scenario: Player ELO = 2100, topic = "DP", difficulty = "HARD"
 *
 * Level 1: getAdaptiveQuestion("DP", "HARD", 2100)
 *   → Searches questions WHERE topic="DP" AND difficulty="HARD"
 *     AND minElo <= 2100 AND maxElo >= 2100
 *   → Our DP HARD question has maxElo = 1800
 *   → 1800 < 2100 → NO MATCH
 *   → perfectMatch = null → continue to Level 2
 *
 * Level 2: getAdaptiveQuestion("DP", "HARD", 1200)
 *   → Same query but with ELO = 1200 (neutral)
 *   → minElo=1500 <= 1200? NO → still no match
 *   → relaxedEloMatch = null → continue to Level 3
 *
 * Level 3: try adjacent difficulty "MEDIUM"
 *   getAdaptiveQuestion("DP", "MEDIUM", 1200)
 *   → DP MEDIUM question: minElo=1200, maxElo=1800
 *   → minElo=1200 <= 1200 ✓ AND maxElo=1800 >= 1200 ✓
 *   → MATCH FOUND → return this question ✓
 *
 * Player gets a DP     Medium question instead of DP Hard.
 * Not perfect but better than crashing or showing nothing.
 *
 * ── WHY seedQuestionsIfNeeded CHECKS COUNT ────────────────────────
 *
 * Alternative approach: use AppDatabase.addCallback(onCreate)
 * Problem: onCreate only fires when DB file is FIRST CREATED.
 *          If user clears app data → DB recreated → questions lost
 *          → onCreate fires again → questions re-seeded ✓
 *
 * BUT: if we add questions to QuestionSeeder in an update,
 *      existing users won't get new questions because
 *      onCreate won't fire for them (DB already exists).
 *
 * Better approach: seedQuestionsIfNeeded() checks count every launch.
 * We can extend this to check count vs expected count:
 *
 *   if (existingCount < QuestionSeeder.questions.size) {
 *       // New questions added in update — insert them
 *       questionDao.insertQuestions(QuestionSeeder.questions)
 *       // IGNORE conflict strategy handles existing ones safely
 *   }
 *
 * This way existing users automatically get new questions
 * when you release an update with more questions added.
 */