package com.example.dsa_duel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FILE: data/QuestionEntity.kt
 *
 * This is a Room Entity — it maps directly to a table called "questions"
 * in your local SQLite database. Every field here becomes a column.
 *
 * Think of it like:
 *   QuestionEntity = one row in the "questions" table
 */
@Entity(tableName = "questions")
data class QuestionEntity(

    // ── Primary Key ───────────────────────────────────────────────
    // Every table needs a unique ID for each row.
    // autoGenerate = false because we manually assign IDs
    // when we seed the question bank (q1, q2, q3...)
    @PrimaryKey
    val id: Int,

    // ── Question content ──────────────────────────────────────────

    // Short title shown at top of duel screen
    // Example: "Find duplicate in array"
    val title: String,

    // Full problem description shown to player
    // Example: "Given an array of n+1 integers where each integer is
    //           between 1 and n, find the duplicate number."
    val description: String,

    // ── Categorization ────────────────────────────────────────────

    // Which DSA topic this question belongs to
    // Values: "Arrays", "LinkedList", "Trees", "Graphs",
    //         "DP", "Strings", "Stacks", "Sorting", "BinarySearch"
    val topic: String,

    // How hard this question is
    // Values: "EASY", "MEDIUM", "HARD"
    val difficulty: String,

    // ── Answer options (MCQ format) ───────────────────────────────
    // We use MCQ so the duel can be answered quickly under time pressure
    // Each option is a full string answer, not just A/B/C/D labels

    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,

    // Which option is correct — stores "A", "B", "C", or "D"
    // Example: if optionB is correct → correctAnswer = "B"
    val correctAnswer: String,

    // Shown after duel ends — explains WHY that answer is correct
    // Example: "Floyd's cycle detection runs in O(n) time and O(1) space"
    val explanation: String,

    // ── ELO range ─────────────────────────────────────────────────
    // Controls which players see this question.
    // Only shown to players whose ELO is between minElo and maxElo.
    // This way beginners don't get hard questions and experts
    // don't waste time on trivial ones.
    //
    // EASY questions:   minElo=800,  maxElo=1300
    // MEDIUM questions: minElo=1000, maxElo=1700
    // HARD questions:   minElo=1400, maxElo=3000
    val minElo: Int,
    val maxElo: Int,

    // ── Time limit ────────────────────────────────────────────────
    // How many seconds the player has to answer this question.
    // EASY = 120s, MEDIUM = 90s, HARD = 60s
    val timeLimitSeconds: Int = 120
)

/*
 * ── HOW THIS CONNECTS TO THE REST OF THE APP ─────────────────────
 *
 * QuestionEntity (this file)
 *       ↓ used by
 * QuestionDao.kt  (next file — defines how to READ/WRITE questions)
 *       ↓ used by
 * AppDatabase.kt  (registers this entity with Room)
 *       ↓ used by
 * QuestionRepository.kt  (business logic layer)
 *       ↓ used by
 * AdaptiveQuestionSelector.kt  (picks the right question for each player)
 *       ↓ used by
 * DuelViewModel.kt  (drives the duel screen)
 *
 * ── EXAMPLE QUESTION ROW ─────────────────────────────────────────
 *
 * QuestionEntity(
 *   id = 1,
 *   title = "Two Sum",
 *   description = "Given array nums and target, return indices of two
 *                  numbers that add up to target.",
 *   topic = "Arrays",
 *   difficulty = "EASY",
 *   optionA = "Use nested loops — O(n²)",
 *   optionB = "Use HashMap — O(n)",
 *   optionC = "Sort the array first — O(n log n)",
 *   optionD = "Use binary search — O(n log n)",
 *   correctAnswer = "B",
 *   explanation = "HashMap stores each number as key and index as value.
 *                  For each element, check if (target - element) exists
 *                  in the map. This gives O(n) time, O(n) space.",
 *   minElo = 800,
 *   maxElo = 1300,
 *   timeLimitSeconds = 120
 * )
 */