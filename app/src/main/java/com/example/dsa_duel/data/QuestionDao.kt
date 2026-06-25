package com.example.dsa_duel.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * FILE: data/QuestionDao.kt
 *
 * DAO = Data Access Object
 *
 * This interface defines every database operation related to questions.
 * Room reads this interface at compile time and AUTO-GENERATES the actual
 * implementation for you. You never write SQL manually against SQLite —
 * you just write the @Query annotation and Room handles the rest.
 *
 * Simple mental model:
 *   QuestionDao = the "menu" of things you can do with the questions table
 *   Room        = the "kitchen" that actually executes those operations
 *
 * IMPORTANT RULES:
 *   - Every function that reads/writes DB must be either:
 *       suspend fun  → for one-time operations (called from coroutine)
 *       Flow<T>      → for real-time observing (auto-updates on change)
 *   - Never call DAO functions from the main thread — always use coroutines
 */
@Dao
interface QuestionDao {

    // ── INSERT ────────────────────────────────────────────────────

    /**
     * Inserts a list of questions into the database.
     * Called ONCE on app first launch to seed the question bank.
     *
     * OnConflictStrategy.IGNORE = if question with same ID already
     * exists, skip it. This prevents duplicates if app restarts.
     * 
     * NOTE: Returning List<Long> instead of Unit to avoid KSP "unexpected jvm signature V" bug in Room 2.6.1.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    // ── FETCH FOR ADAPTIVE ALGORITHM ──────────────────────────────

    /**
     * The MOST IMPORTANT query in the whole app.
     *
     * Used by AdaptiveQuestionSelector to pick the right question.
     * Filters by:
     *   - topic     → only questions from the selected DSA topic
     *   - difficulty → only questions matching player's level
     *   - ELO range → only questions appropriate for player's ELO
     *
     * ORDER BY RANDOM() → picks a different question each time
     * LIMIT 1           → we only need one question per duel
     *
     * Example call:
     *   getAdaptiveQuestion("Arrays", "MEDIUM", 1100, 1600)
     *   → returns one random Arrays/Medium question for ~1300 ELO player
     */
    @Query("""
        SELECT * FROM questions
        WHERE topic = :topic
        AND difficulty = :difficulty
        AND minElo <= :playerElo
        AND maxElo >= :playerElo
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getAdaptiveQuestion(
        topic: String,
        difficulty: String,
        playerElo: Int
    ): QuestionEntity?

    /**
     * Fallback query — if adaptive selector finds no match,
     * just grab any question from the topic regardless of ELO.
     * This prevents the duel from crashing when question bank is small.
     */
    @Query("""
        SELECT * FROM questions
        WHERE topic = :topic
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomQuestionByTopic(topic: String): QuestionEntity?

    /**
     * Last resort fallback — grab literally any question.
     * Used when topic + difficulty combination has no questions yet.
     */
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1")
    suspend fun getAnyRandomQuestion(): QuestionEntity?

    // ── FETCH FOR UI DISPLAY ──────────────────────────────────────

    /**
     * Returns ALL questions as a Flow.
     * Flow means: every time questions table changes,
     * this automatically emits the updated list.
     *
     * Used by: question browser screen (future feature)
     */
    @Query("SELECT * FROM questions ORDER BY topic, difficulty")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    /**
     * Returns all questions for a specific topic.
     * Used by: Topic Mastery screen to show question list per topic.
     *
     * Example: getAllByTopic("Arrays") → all array questions
     */
    @Query("""
        SELECT * FROM questions
        WHERE topic = :topic
        ORDER BY difficulty
    """)
    fun getAllByTopic(topic: String): Flow<List<QuestionEntity>>

    /**
     * Returns a specific question by its ID.
     * Used by: Result screen to show the correct answer explanation
     * after a duel ends.
     */
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Int): QuestionEntity?

    // ── STATS / COUNTS ────────────────────────────────────────────

    /**
     * Returns total number of questions in the database.
     * Used by: AppDatabase seeder to check if questions
     * are already loaded (avoids re-seeding on every launch).
     *
     * Logic: if count > 0 → already seeded → skip seeding
     */
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    /**
     * Returns count of questions per topic.
     * Used by: Topic Mastery screen to show "17/20 qs" progress.
     *
     * Example: getCountByTopic("Arrays") → 20
     */
    @Query("SELECT COUNT(*) FROM questions WHERE topic = :topic")
    suspend fun getCountByTopic(topic: String): Int

    /**
     * Returns all distinct topics in the database.
     * Used by: Roadmap screen and topic picker to build
     * the list dynamically from whatever topics exist in DB.
     *
     * Returns: ["Arrays", "LinkedList", "Trees", "Graphs", ...]
     */
    @Query("SELECT DISTINCT topic FROM questions ORDER BY topic")
    suspend fun getAllTopics(): List<String>

    /**
     * Returns all distinct topics as a Flow (live updates).
     * Used by: Duel setup screen topic chip selector.
     */
    @Query("SELECT DISTINCT topic FROM questions ORDER BY topic")
    fun getAllTopicsFlow(): Flow<List<String>>

    // ── DELETE (admin / debug use) ────────────────────────────────

    /**
     * Deletes ALL questions from the table.
     * Only used during development to reset and re-seed.
     * DO NOT call this in production code.
     * 
     * NOTE: Returning Int instead of Unit to avoid KSP "unexpected jvm signature V" bug in Room 2.6.1.
     */
    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions(): Int
}

/*
 * ── HOW QUERIES WORK IN ROOM ──────────────────────────────────────
 *
 * Room compiles your @Query SQL at BUILD TIME.
 * If your SQL has a typo → your project won't compile.
 * This is actually a safety feature — SQL errors caught at compile
 * time, not at runtime when the user is using the app.
 *
 * ── SUSPEND VS FLOW ───────────────────────────────────────────────
 *
 * suspend fun getById(id)     → one-time read, returns once
 * fun getAllQuestions(): Flow  → live stream, re-emits on every DB change
 *
 * Rule of thumb:
 *   - Reading for duel logic  → suspend fun  (one-time)
 *   - Reading for UI display  → Flow<T>      (stays fresh automatically)
 *
 * ── EXAMPLE USAGE IN VIEWMODEL ───────────────────────────────────
 *
 * viewModelScope.launch {
 *     val question = questionDao.getAdaptiveQuestion(
 *         topic      = "Arrays",
 *         difficulty = "MEDIUM",
 *         playerElo  = 1350
 *     )
 *     // question is now a QuestionEntity? (nullable — might be null
 *     // if no matching question found)
 * }
 */
