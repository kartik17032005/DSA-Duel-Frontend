package com.example.dsa_duel.repositories

import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.QuestionDao
import com.example.dsa_duel.data.QuestionEntity
import com.example.dsa_duel.data.TopicMasteryDao
import com.example.dsa_duel.models.BotProfile
import com.example.dsa_duel.models.DuelResult
import com.example.dsa_duel.utils.AdaptiveQuestionSelector
import com.example.dsa_duel.utils.BotGenerator
import com.example.dsa_duel.utils.EloEngine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: repositories/DuelRepository.kt
 *
 * The "conductor" repository — orchestrates the entire duel session.
 *
 * ANALOGY:
 * Think of DuelRepository as a restaurant manager:
 *   - AdaptiveQuestionSelector = chef (picks the dish/question)
 *   - BotGenerator             = host (assigns you a table/opponent)
 *   - EloEngine                = accountant (calculates the bill/ELO)
 *   - StatsRepository          = cashier (saves the transaction)
 *   - DuelRepository           = manager (coordinates everyone)
 *
 * DuelViewModel only talks to DuelRepository.
 * DuelRepository talks to everything else.
 * This keeps DuelViewModel clean and focused on UI state only.
 *
 * COMPLETE DUEL LIFECYCLE:
 *
 *   prepareDuel()     → pick topic, difficulty, question, generate bot
 *       ↓
 *   getEloStakes()    → calculate potential gain/loss before duel starts
 *       ↓
 *   submitAnswer()    → determine win/loss, calculate actual ELO change
 *       ↓
 *   saveDuelResult()  → persist everything to Room via StatsRepository
 */
@Singleton
class DuelRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val playerStatsDao: PlayerStatsDao,
    private val topicMasteryDao: TopicMasteryDao,
    private val statsRepository: StatsRepository
) {

    // Lazy-initialized utilities — created once, reused forever
    private val questionRepository = QuestionRepository(questionDao)
    private val adaptiveSelector   = AdaptiveQuestionSelector(
        topicMasteryDao = topicMasteryDao,
        playerStatsDao  = playerStatsDao
    )

    // ── STEP 1: PREPARE DUEL ─────────────────────────────────────

    /**
     * Prepares everything needed before showing the VS screen.
     *
     * Called by DuelViewModel when user taps "FIND OPPONENT".
     * Runs on IO thread — never call from main thread.
     *
     * What happens inside:
     *   1. Seed questions if this is first launch
     *   2. Get player's current ELO from DB
     *   3. Adaptive algorithm picks the best topic
     *   4. Determine difficulty based on ELO
     *   5. Fetch a question matching topic + difficulty + ELO
     *   6. Generate a bot opponent near player's ELO
     *   7. Calculate potential ELO gain/loss
     *   8. Return everything packaged as DuelPreparation
     *
     * Returns [DuelPreparation] — a data class holding everything
     * DuelViewModel needs to show the MatchFound state.
     *
     * Throws [QuestionNotFoundException] if no questions in DB.
     */
    suspend fun prepareDuel(): DuelPreparation {
        // ── Step 1: Ensure questions are seeded ───────────────────
        questionRepository.seedQuestionsIfNeeded()

        // ── Step 2: Get player's current ELO ─────────────────────
        val playerElo = statsRepository.getCurrentElo()

        // ── Step 3: Adaptive algorithm picks topic ────────────────
        // Finds the topic where player needs the most work
        // (highest adaptive priority from TopicMasteryEntity)
        val selectedTopic = adaptiveSelector.selectBestTopic()

        // ── Step 4: Determine difficulty from ELO ────────────────
        val difficulty = determineDifficulty(playerElo)

        // ── Step 5: Fetch matching question ───────────────────────
        // Uses the full fallback chain from QuestionRepository
        val question = questionRepository.getQuestionForDuel(
            topic      = selectedTopic,
            difficulty = difficulty,
            playerElo  = playerElo
        )

        // ── Step 6: Generate bot opponent ─────────────────────────
        val bot = BotGenerator.generateBot(playerElo)

        // ── Step 7: Calculate ELO stakes ─────────────────────────
        val potentialGain = EloEngine.eloGain(playerElo, bot.eloRating)
        val potentialLoss = EloEngine.eloLoss(playerElo, bot.eloRating)

        return DuelPreparation(
            question     = question,
            bot          = bot,
            playerElo    = playerElo,
            potentialGain = potentialGain,
            potentialLoss = potentialLoss,
            difficulty   = difficulty
        )
    }

    // ── STEP 2: SUBMIT ANSWER ─────────────────────────────────────

    /**
     * Called when player taps an answer option OR timer runs out.
     *
     * Determines if player won by checking:
     *   - Was player's answer correct?
     *   - Did bot answer before player? (bot won)
     *   - Did time run out? (bot wins by default)
     *
     * Then saves the result to DB via StatsRepository.
     *
     * Parameters:
     *   [selectedOption]   → "A", "B", "C", "D" or null (timeout)
     *   [question]         → the question that was asked
     *   [bot]              → the bot opponent
     *   [playerElo]        → player ELO at start of duel
     *   [botAnsweredFirst] → true if bot "answered" before player
     *   [timeTakenSeconds] → how many seconds player took
     *   [wasTimeout]       → true if timer hit zero
     *
     * Returns [DuelOutcome] — everything needed for ResultScreen.
     */
    suspend fun submitAnswer(
        selectedOption: String?,
        question: QuestionEntity,
        bot: BotProfile,
        playerElo: Int,
        botAnsweredFirst: Boolean,
        timeTakenSeconds: Int,
        wasTimeout: Boolean
    ): DuelOutcome {

        // ── Determine if player answered correctly ────────────────
        val playerAnsweredCorrectly = selectedOption != null &&
                selectedOption == question.correctAnswer

        // ── Determine winner ──────────────────────────────────────
        // Player wins ONLY if:
        //   1. Player answered correctly AND
        //   2. Bot did not answer before player
        val playerWon = playerAnsweredCorrectly && !botAnsweredFirst && !wasTimeout

        // ── Check if bot answered correctly ───────────────────────
        // Bot "answers" based on its accuracy probability.
        // This is pre-calculated in BotGenerator — bot's accuracy
        // is a Float between 0.0 and 1.0.
        // We check randomly against accuracy when bot "answers".
        val botAnsweredCorrectly = botAnsweredFirst &&
                (Math.random() < bot.accuracy)

        // ── Save result and get ELO change ────────────────────────
        // StatsRepository handles:
        //   - ELO calculation via EloEngine
        //   - Updating player_stats row
        //   - Updating topic_mastery row
        //   - Checking topic unlocks
        val eloChange = statsRepository.processDuelResult(
            playerWon  = playerWon,
            botElo     = bot.eloRating,
            topic      = question.topic,
            wasCorrect = playerAnsweredCorrectly,
            wasTimeout = wasTimeout
        )

        // ── Get new ELO for display ───────────────────────────────
        val newElo = playerElo + eloChange

        // ── Build DuelResult for history ──────────────────────────
        // Lightweight record saved for Recent Battles list
        val duelResult = DuelResult(
            topic             = question.topic,
            difficulty        = question.difficulty,
            playerWon         = playerWon,
            eloChange         = eloChange,
            botName           = bot.name,
            timeTakenSeconds  = timeTakenSeconds
        )

        return DuelOutcome(
            playerWon             = playerWon,
            playerAnsweredCorrectly = playerAnsweredCorrectly,
            botAnsweredCorrectly  = botAnsweredCorrectly,
            playerAnswer          = selectedOption ?: "TIMEOUT",
            correctAnswer         = question.correctAnswer,
            explanation           = question.explanation,
            eloChange             = eloChange,
            newElo                = newElo,
            wasTimeout            = wasTimeout,
            duelResult            = duelResult
        )
    }

    // ── HELPER: DETERMINE DIFFICULTY ─────────────────────────────

    /**
     * Maps player ELO to question difficulty.
     *
     * ELO ranges:
     *   0    - 1099  → EASY   (learning fundamentals)
     *   1100 - 1499  → MEDIUM (developing proficiency)
     *   1500+        → HARD   (competitive level)
     *
     * These thresholds are intentionally overlapping with question
     * ELO ranges so there's always something available.
     *
     * A player at exactly 1100 gets MEDIUM questions —
     * this is a small push beyond their comfort zone,
     * which is where the most learning happens.
     */
    private fun determineDifficulty(playerElo: Int): String {
        return when {
            playerElo < 1100 -> "EASY"
            playerElo < 1500 -> "MEDIUM"
            else             -> "HARD"
        }
    }

    // ── UTILITY: RECENT BATTLES ───────────────────────────────────

    /**
     * Returns recent duel results for Home screen.
     * Currently returns from in-memory list (future: from Room).
     *
     * In Phase 2, this will query a DuelHistoryDao
     * that stores every DuelResult in a separate Room table.
     */
    fun observeRecentBattles() = statsRepository.observePlayerStats()
}

// ── DATA CLASSES RETURNED BY DuelRepository ───────────────────────

/**
 * Everything needed to show the MatchFound (VS) screen
 * and start the InProgress (question) screen.
 *
 * Produced by: prepareDuel()
 * Consumed by: DuelViewModel → DuelState.MatchFound + DuelState.InProgress
 */
data class DuelPreparation(
    val question: QuestionEntity,       // the question for this duel
    val bot: BotProfile,                // the generated bot opponent
    val playerElo: Int,                 // player's ELO at start
    val potentialGain: Int,             // ELO gained if player wins (+24)
    val potentialLoss: Int,             // ELO lost if player loses (-19)
    val difficulty: String              // "EASY" / "MEDIUM" / "HARD"
)

/**
 * Everything needed to show the Result screen.
 *
 * Produced by: submitAnswer()
 * Consumed by: DuelViewModel → DuelState.Result
 */
data class DuelOutcome(
    val playerWon: Boolean,             // true = WIN banner
    val playerAnsweredCorrectly: Boolean, // player's answer was right
    val botAnsweredCorrectly: Boolean,  // bot's answer was right
    val playerAnswer: String,           // "A","B","C","D" or "TIMEOUT"
    val correctAnswer: String,          // the right answer
    val explanation: String,            // why correct answer is right
    val eloChange: Int,                 // + or - number
    val newElo: Int,                    // player's ELO after duel
    val wasTimeout: Boolean,            // ran out of time?
    val duelResult: DuelResult          // lightweight history record
)

/*
 * ── HOW DuelViewModel USES THIS REPOSITORY ────────────────────────
 *
 * fun findDuel() {
 *     viewModelScope.launch {
 *         _state.value = DuelState.FindingMatch(playerElo)
 *         delay(2000)  // matchmaking feel
 *
 *         try {
 *             val prep = duelRepository.prepareDuel()
 *             _state.value = DuelState.MatchFound(
 *                 bot           = prep.bot,
 *                 question      = prep.question,
 *                 potentialGain = prep.potentialGain,
 *                 potentialLoss = prep.potentialLoss
 *             )
 *         } catch (e: QuestionNotFoundException) {
 *             _state.value = DuelState.Error(e.message ?: "No questions found")
 *         }
 *     }
 * }
 *
 * fun submitAnswer(option: String) {
 *     viewModelScope.launch {
 *         val outcome = duelRepository.submitAnswer(
 *             selectedOption   = option,
 *             question         = currentQuestion,
 *             bot              = currentBot,
 *             playerElo        = currentPlayerElo,
 *             botAnsweredFirst = botAlreadyAnswered,
 *             timeTakenSeconds = totalTime - timeLeft,
 *             wasTimeout       = false
 *         )
 *         _state.value = DuelState.Result(
 *             playerWon     = outcome.playerWon,
 *             eloChange     = outcome.eloChange,
 *             newElo        = outcome.newElo,
 *             explanation   = outcome.explanation,
 *             ...
 *         )
 *     }
 * }
 *
 * ── WHY DUELREPOSITORY TAKES 4 PARAMETERS IN CONSTRUCTOR ─────────
 *
 * DuelRepository @Inject constructor(
 *     questionDao: QuestionDao,
 *     playerStatsDao: PlayerStatsDao,
 *     topicMasteryDao: TopicMasteryDao,
 *     statsRepository: StatsRepository
 * )
 *
 * It needs:
 *   questionDao      → to pass to QuestionRepository internally
 *   playerStatsDao   → to pass to AdaptiveQuestionSelector
 *   topicMasteryDao  → to pass to AdaptiveQuestionSelector
 *   statsRepository  → to call processDuelResult() after duel
 *
 * Hilt injects all four automatically via AppModule.kt.
 * DuelViewModel only sees DuelRepository — not any of these four.
 * This is the power of the repository pattern:
 * complete encapsulation of the data layer from the UI layer.
 */