package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.QuestionEntity
import com.example.dsa_duel.models.BotProfile
import com.example.dsa_duel.models.DuelState
import com.example.dsa_duel.repositories.DuelPreparation
import com.example.dsa_duel.repositories.DuelRepository
import com.example.dsa_duel.repositories.QuestionNotFoundException
import com.example.dsa_duel.utils.BotGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * FILE: viewModels/DuelViewModel.kt
 */
@HiltViewModel
class DuelViewModel @Inject constructor(
    private val duelRepository: DuelRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DuelState>(DuelState.Idle)
    val state: StateFlow<DuelState> = _state.asStateFlow()

    private var currentQuestion: QuestionEntity? = null
    private var currentBot: BotProfile?          = null
    private var currentPlayerElo: Int            = 1200
    private var totalTimeSeconds: Int            = 120
    private var botAlreadyAnswered: Boolean      = false
    private var timeWhenPlayerAnswered: Int      = 0

    private var matchmakingJob: Job? = null
    private var timerJob: Job?       = null

    /**
     * Called when user taps "FIND OPPONENT".
     *
     * In the current local-bot version, this simulates a network search.
     */
    fun findDuel() {
        matchmakingJob?.cancel()

        matchmakingJob = viewModelScope.launch {
            resetSessionData()

            // 1. Generate a temporary ID for visual feedback on the scanning screen
            val tempId = "UID-" + Random.nextInt(1000, 9999).toString()

            // 2. Show matchmaking screen immediately
            _state.value = DuelState.FindingMatch(
                playerElo = currentPlayerElo,
                myUid = tempId,
                queueSize = Random.nextInt(12, 45) // Cosmetic "Online" count
            )

            try {
                // 3. Prepare duel (Seeds DB if first run, fetches question & bot)
                val preparation = duelRepository.prepareDuel()

                // Sync local data
                currentQuestion  = preparation.question
                currentBot       = preparation.bot
                currentPlayerElo = preparation.playerElo
                totalTimeSeconds = preparation.question.timeLimitSeconds

                // 4. Force a minimum 2.5s delay so the scanning animation is visible
                delay(2500L)

                // 5. Transition to VS screen
                _state.value = DuelState.MatchFound(
                    myElo         = preparation.playerElo,
                    myName        = "YOU", 
                    opponent      = preparation.bot,
                    question      = preparation.question,
                    potentialGain = preparation.potentialGain,
                    potentialLoss = preparation.potentialLoss,
                    isBot         = true,
                    isMeReady     = false,
                    isOpponentReady = true
                )

            } catch (e: QuestionNotFoundException) {
                _state.value = DuelState.Error(
                    message = "No questions available. Please restart the app."
                )
            } catch (e: Exception) {
                _state.value = DuelState.Error(
                    message = "Connection Error: ${e.message ?: "Server unreachable"}"
                )
            }
        }
    }

    fun cancelMatchmaking() {
        matchmakingJob?.cancel()
        _state.value = DuelState.Idle
    }

    fun startDuel() {
        val question = currentQuestion ?: run {
            _state.value = DuelState.Error("Duel data lost. Please try again.")
            return
        }
        val bot = currentBot ?: run {
            _state.value = DuelState.Error("Opponent data lost. Please try again.")
            return
        }

        botAlreadyAnswered = false
        val botAnswerAtTimeLeft = (totalTimeSeconds - bot.solveTimeSeconds)
            .coerceIn(5, totalTimeSeconds - 5)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (timeLeft in totalTimeSeconds downTo 0) {
                _state.update { currentState ->
                    if (currentState is DuelState.InProgress) {
                        currentState.copy(timeLeftSeconds = timeLeft)
                    } else currentState
                }

                if (timeLeft == totalTimeSeconds) {
                    _state.value = DuelState.InProgress(
                        question         = question,
                        opponent         = bot,
                        timeLeftSeconds  = timeLeft,
                        opponentSolvesAtSecond = botAnswerAtTimeLeft,
                        selectedOption   = null,
                        playerElo        = currentPlayerElo,
                        opponentElo      = bot.eloRating
                    )
                }

                if (timeLeft == botAnswerAtTimeLeft && !botAlreadyAnswered) {
                    botAlreadyAnswered = true
                    handleBotAnswer(bot, question, timeLeft)
                    if (_state.value is DuelState.Result) return@launch
                }

                if (timeLeft == 0) {
                    handleTimeout(question, bot)
                    return@launch
                }
                delay(1000L)
            }
        }
    }

    fun submitAnswer(selectedOption: String) {
        val currentState = _state.value
        if (currentState !is DuelState.InProgress) return
        if (currentState.selectedOption != null) return

        timerJob?.cancel()
        val timeTaken = totalTimeSeconds - currentState.timeLeftSeconds
        timeWhenPlayerAnswered = timeTaken

        _state.update {
            if (it is DuelState.InProgress) it.copy(selectedOption = selectedOption) else it
        }

        viewModelScope.launch {
            processResult(selectedOption, currentState.question, currentState.opponent, botAlreadyAnswered, timeTaken, false)
        }
    }

    private suspend fun handleBotAnswer(bot: BotProfile, question: QuestionEntity, timeLeft: Int) {
        if (BotGenerator.didBotAnswerCorrectly(bot.accuracy)) {
            processResult(null, question, bot, true, totalTimeSeconds - timeLeft, false)
        }
    }

    private suspend fun handleTimeout(question: QuestionEntity, bot: BotProfile) {
        processResult(null, question, bot, botAlreadyAnswered, totalTimeSeconds, true)
    }

    private suspend fun processResult(
        selectedOption: String?,
        question: QuestionEntity,
        bot: BotProfile,
        botAnsweredFirst: Boolean,
        timeTaken: Int,
        wasTimeout: Boolean
    ) {
        try {
            val outcome = duelRepository.submitAnswer(
                selectedOption, question, bot, currentPlayerElo, botAnsweredFirst, timeTaken, wasTimeout
            )

            _state.value = DuelState.Result(
                playerWon = outcome.playerWon,
                playerAnswer = outcome.playerAnswer,
                correctAnswer = outcome.correctAnswer,
                explanation = outcome.explanation,
                eloChange = outcome.eloChange,
                newElo = outcome.newElo,
                opponent = bot,
                timeTakenSeconds = timeTaken,
                wasTimeout = wasTimeout,
                question = question
            )
        } catch (e: Exception) {
            _state.value = DuelState.Error("Failed to save result: ${e.message}")
        }
    }

    fun resetDuel() {
        timerJob?.cancel()
        matchmakingJob?.cancel()
        resetSessionData()
        _state.value = DuelState.Idle
    }

    private fun resetSessionData() {
        currentQuestion = null
        currentBot = null
        botAlreadyAnswered = false
        timeWhenPlayerAnswered = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchmakingJob?.cancel()
    }
}
