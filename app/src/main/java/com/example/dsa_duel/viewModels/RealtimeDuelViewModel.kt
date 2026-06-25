package com.example.dsa_duel.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.QuestionEntity
import com.example.dsa_duel.models.BotProfile
import com.example.dsa_duel.models.DuelState
import com.example.dsa_duel.repositories.DuelRoom
import com.example.dsa_duel.repositories.MatchResult
import com.example.dsa_duel.repositories.QueueEntry
import com.example.dsa_duel.repositories.RealtimeDuelRepository
import com.example.dsa_duel.utils.BotGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RealtimeDuelViewModel @Inject constructor(
    private val repository: RealtimeDuelRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val statsDao: PlayerStatsDao
) : ViewModel() {

    private val _state = MutableStateFlow<DuelState>(DuelState.Idle)
    val state: StateFlow<DuelState> = _state.asStateFlow()

    private var currentRoom: DuelRoom? = null
    private var myRole: String = "player1"
    private val myUid get() = auth.currentUser?.uid ?: ""
    private val isP1 get() = myRole == "player1"

    private var searchJob: Job? = null
    private var timerJob: Job? = null
    private var roomListenerJob: Job? = null
    private var queueListenerJob: Job? = null

    private var localElo: Int = 1200
    private var localRankEmoji: String = "🥉"
    private var localDisplayName: String = "Warrior"

    init {
        viewModelScope.launch {
            val stats = statsDao.getStats()
            localElo = stats?.eloRating ?: 1200
            localRankEmoji = stats?.rankEmoji ?: "🥉"
            localDisplayName = stats?.displayName ?: "Warrior"
        }
    }

    fun findDuel() {
        if (_state.value !is DuelState.Idle) return
        _state.value = DuelState.FindingMatch(playerElo = localElo, myUid = myUid, statusMessage = "INITIALIZING...")
        
        viewModelScope.launch {
            try {
                if (auth.currentUser == null) {
                    _state.update { if (it is DuelState.FindingMatch) it.copy(statusMessage = "AUTHENTICATING...") else it }
                    auth.signInAnonymously().await()
                    _state.update { 
                        if (it is DuelState.FindingMatch) it.copy(myUid = myUid, statusMessage = "CONNECTED") 
                        else it 
                    }
                }
                
                _state.update { if (it is DuelState.FindingMatch) it.copy(statusMessage = "JOINING QUEUE...") else it }
                startMatchmaking()
            } catch (e: Exception) {
                Log.e("DuelVM", "FindDuel error", e)
                _state.value = DuelState.Error("Matchmaking Failed: ${e.localizedMessage}")
            }
        }
    }

    fun cancelMatchmaking() {
        searchJob?.cancel(); queueListenerJob?.cancel()
        viewModelScope.launch {
            if (myUid.isNotEmpty()) repository.leaveQueue(myUid)
            _state.value = DuelState.Idle
        }
    }

    fun startDuel() {
        val s = _state.value as? DuelState.MatchFound ?: return
        if (s.isMeReady) return

        if (s.isBot) {
            _state.value = DuelState.InProgress(
                question = s.question,
                opponent = s.opponent,
                timeLeftSeconds = s.question.timeLimitSeconds,
                opponentSolvesAtSecond = (5..s.question.timeLimitSeconds).random(),
                selectedOption = null,
                playerElo = localElo,
                opponentElo = s.opponent.eloRating
            )
            startBotTimer(s.question, s.opponent)
        } else {
            // Update local state immediately for UI feedback
            _state.value = s.copy(isMeReady = true)
            val room = currentRoom ?: return
            viewModelScope.launch { repository.setPlayerReady(room.roomId, isP1) }
        }
    }

    private fun startBotTimer(question: QuestionEntity, bot: BotProfile) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var timeLeft = question.timeLimitSeconds
            val solveAtRange = (timeLeft * 0.3).toInt().. (timeLeft * 0.9).toInt()
            val botSolvesAt = solveAtRange.random()

            while (timeLeft >= 0) {
                val current = _state.value as? DuelState.InProgress ?: break
                _state.value = current.copy(timeLeftSeconds = timeLeft)
                delay(1000L)
                timeLeft--
            }
            if (_state.value is DuelState.InProgress) {
                 showBotResult(question, bot)
            }
        }
    }

    private fun showBotResult(question: QuestionEntity, bot: BotProfile) {
        val current = _state.value as? DuelState.InProgress
        val myAnswer = current?.selectedOption
        val won = myAnswer == question.correctAnswer
        val eloChange = if (won) 20 else -15
        val newElo = (localElo + eloChange).coerceAtLeast(100)
        
        _state.value = DuelState.Result(
            playerWon = won,
            playerAnswer = myAnswer ?: "TIMEOUT",
            correctAnswer = question.correctAnswer,
            explanation = question.explanation,
            eloChange = eloChange,
            newElo = newElo,
            opponent = bot,
            timeTakenSeconds = question.timeLimitSeconds - (current?.timeLeftSeconds ?: 0),
            wasTimeout = myAnswer == null,
            question = question
        )
        
        viewModelScope.launch {
            repository.applyEloChange(myUid, eloChange, localElo)
            statsDao.updateAfterDuel(won = if (won) 1 else 0, newElo = newElo, todayMs = System.currentTimeMillis(), wasTimeout = if (myAnswer == null) 1 else 0, newStreak = 0, newLongestStreak = 0)
        }
    }

    fun submitAnswer(answer: String) {
        val s = _state.value as? DuelState.InProgress ?: return
        if (s.selectedOption != null) return
        _state.value = s.copy(selectedOption = answer)
        
        val room = currentRoom
        if (room != null) {
            viewModelScope.launch { repository.submitAnswer(room.roomId, isP1, answer) }
        }
    }

    fun resetDuel() { cleanup(); _state.value = DuelState.Idle }

    private fun startMatchmaking() {
        val uid = myUid
        val myEntry = QueueEntry(uid, localDisplayName, localElo, localRankEmoji)
        val startTime = System.currentTimeMillis()
        
        searchJob = viewModelScope.launch {
            repository.joinQueue(myEntry)
            
            queueListenerJob = launch {
                repository.listenToQueueDoc(uid).collect { result ->
                    if (result is MatchResult.Matched && _state.value is DuelState.FindingMatch) {
                        myRole = if (result.isPlayer1) "player1" else "player2"
                        subscribeToRoom(result.roomId)
                    }
                }
            }

            while (_state.value is DuelState.FindingMatch) {
                val elapsed = System.currentTimeMillis() - startTime
                // Increase bot fallback to 25 seconds for better real matchmaking chance
                if (elapsed > 25000L) {
                    _state.update { if (it is DuelState.FindingMatch) it.copy(statusMessage = "SIMULATING OPPONENT...") else it }
                    delay(1000L)
                    startBotDuel()
                    break
                }

                _state.update { if (it is DuelState.FindingMatch) it.copy(statusMessage = "SEARCHING FOR RIVALS...") else it }
                
                val result = repository.tryFindOpponentAndCreateRoom(myEntry)
                if (result is MatchResult.Matched) {
                    myRole = if (result.isPlayer1) "player1" else "player2"
                    _state.update { if (it is DuelState.FindingMatch) it.copy(statusMessage = "MATCH FOUND!") else it }
                    subscribeToRoom(result.roomId)
                    break
                }
                delay(3000L)
            }
        }
    }

    private fun startBotDuel() {
        viewModelScope.launch {
            try {
                val bot = BotGenerator.generateBot(localElo)
                val q = repository.getFallbackQuestion()
                val question = QuestionEntity(
                    id = q.id.hashCode(), title = q.title, description = q.description,
                    topic = q.topic, difficulty = q.difficulty, optionA = q.optionA,
                    optionB = q.optionB, optionC = q.optionC, optionD = q.optionD,
                    correctAnswer = q.correctAnswer, explanation = q.explanation,
                    minElo = 0, maxElo = 3000, timeLimitSeconds = q.timeLimitSeconds
                )
                repository.leaveQueue(myUid)
                _state.value = DuelState.MatchFound(
                    myElo = localElo,
                    myName = localDisplayName,
                    opponent = bot,
                    question = question,
                    potentialGain = 20,
                    potentialLoss = 15,
                    isBot = true
                )
            } catch (e: Exception) {
                _state.value = DuelState.Error("Bot Simulation Failed: ${e.message}")
            }
        }
    }

    private fun subscribeToRoom(roomId: String) {
        roomListenerJob?.cancel()
        roomListenerJob = repository.listenToRoom(roomId)
            .onEach { room -> room?.let { handleRoomUpdate(it) } }
            .launchIn(viewModelScope)
    }

    private fun handleRoomUpdate(room: DuelRoom) {
        currentRoom = room
        val opponent = if (isP1) room.player2 else room.player1
        val me = if (isP1) room.player1 else room.player2
        
        val q = room.question
        val questionEntity = QuestionEntity(id = q.id.hashCode(), title = q.title, description = q.description, topic = q.topic, difficulty = q.difficulty, optionA = q.optionA, optionB = q.optionB, optionC = q.optionC, optionD = q.optionD, correctAnswer = q.correctAnswer, explanation = q.explanation, minElo = 0, maxElo = 3000, timeLimitSeconds = q.timeLimitSeconds)
        val opponentProfile = BotProfile(name = opponent.displayName, eloRating = opponent.eloRating, solveTimeSeconds = 0, accuracy = 1.0f, rankName = "OPPONENT", rankEmoji = opponent.rankEmoji)

        when (room.status) {
            "waiting" -> {
                _state.value = DuelState.MatchFound(
                    myElo = localElo,
                    myName = localDisplayName,
                    opponent = opponentProfile,
                    question = questionEntity,
                    potentialGain = 25,
                    potentialLoss = 15,
                    isMeReady = me.isReady,
                    isOpponentReady = opponent.isReady,
                    isBot = false
                )
            }
            "in_progress" -> {
                val startedAt = room.startedAtMs ?: return
                val elapsedSec = (System.currentTimeMillis() - startedAt) / 1000L
                val timeLeft = (q.timeLimitSeconds - elapsedSec).toInt().coerceAtLeast(0)
                
                _state.value = DuelState.InProgress(
                    question = questionEntity,
                    opponent = opponentProfile,
                    timeLeftSeconds = timeLeft,
                    opponentSolvesAtSecond = -1,
                    selectedOption = me.answer,
                    playerElo = localElo,
                    opponentElo = opponent.eloRating
                )
                startTimer(room, opponentProfile, questionEntity)
            }
            "finished" -> {
                timerJob?.cancel()
                if (_state.value !is DuelState.Result) showResult(room, opponentProfile, questionEntity)
            }
        }
    }

    private fun startTimer(room: DuelRoom, bot: BotProfile, question: QuestionEntity) {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                val startedAt = room.startedAtMs ?: break
                val elapsedSec = (System.currentTimeMillis() - startedAt) / 1000L
                val timeLeft = (room.question.timeLimitSeconds - elapsedSec).toInt()
                
                if (timeLeft <= 0) {
                    repository.finalizeResult(room.roomId, room)
                    break 
                }
                
                val current = _state.value as? DuelState.InProgress
                if (current != null) {
                    _state.value = current.copy(timeLeftSeconds = timeLeft)
                }
                delay(1000L)
            }
        }
    }

    private fun showResult(room: DuelRoom, bot: BotProfile, question: QuestionEntity) {
        val myEloChange = if (isP1) room.eloChange1 else room.eloChange2
        val newElo = (localElo + myEloChange).coerceAtLeast(100)
        val playerWon = room.winnerId == myUid
        val myAnswer = if (isP1) room.player1.answer else room.player2.answer
        
        val timeTaken = if (room.startedAtMs != null) {
            val answeredAt = (if (isP1) room.player1.answeredAtMs else room.player2.answeredAtMs) ?: System.currentTimeMillis()
            ((answeredAt - room.startedAtMs) / 1000L).toInt()
        } else 0

        _state.value = DuelState.Result(
            playerWon = playerWon,
            playerAnswer = myAnswer ?: "TIMEOUT",
            correctAnswer = room.question.correctAnswer,
            explanation = room.question.explanation,
            eloChange = myEloChange,
            newElo = newElo,
            opponent = bot,
            timeTakenSeconds = timeTaken,
            wasTimeout = myAnswer == null,
            question = question
        )

        viewModelScope.launch {
            try {
                repository.applyEloChange(myUid, myEloChange, localElo)
                statsDao.updateAfterDuel(won = if (playerWon) 1 else 0, newElo = newElo, todayMs = System.currentTimeMillis(), wasTimeout = if (myAnswer == null) 1 else 0, newStreak = 0, newLongestStreak = 0)
            } catch (_: Exception) {}
        }
    }

    private fun cleanup() {
        searchJob?.cancel(); timerJob?.cancel(); roomListenerJob?.cancel(); queueListenerJob?.cancel()
        viewModelScope.launch { if (myUid.isNotEmpty()) repository.leaveQueue(myUid) }
    }

    override fun onCleared() { super.onCleared(); cleanup() }
}
