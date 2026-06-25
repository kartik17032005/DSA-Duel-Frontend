package com.example.dsa_duel.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

// ── Models ─────────────────────────────────

data class Question(
    val id: String = "",
    val topic: String = "",
    val title: String = "",
    val description: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "A",
    val timeLimitSeconds: Int = 60,
    val explanation: String = "",
    val difficulty: String = "MEDIUM"
)

data class QueueEntry(
    val uid: String,
    val displayName: String,
    val eloRating: Int,
    val rankEmoji: String
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "eloRating" to eloRating,
        "rankEmoji" to rankEmoji,
        "status" to "waiting",
        "timestamp" to FieldValue.serverTimestamp()
    )
}

data class RoomPlayer(
    val uid: String = "",
    val displayName: String = "",
    val eloRating: Int = 1200,
    val rankEmoji: String = "🥉",
    val answer: String? = null,
    val answeredAtMs: Long? = null,
    val isReady: Boolean = false
)

data class DuelRoom(
    val roomId: String = "",
    val player1: RoomPlayer = RoomPlayer(),
    val player2: RoomPlayer = RoomPlayer(),
    val question: Question = Question(),
    val status: String = "waiting",
    val startedAtMs: Long? = null,
    val winnerId: String? = null,
    val eloChange1: Int = 0,
    val eloChange2: Int = 0,
    val createdAtMs: Long = 0L
)

sealed class MatchResult {
    data class Matched(val roomId: String, val isPlayer1: Boolean) : MatchResult()
    data object StillSearching : MatchResult()
    data class Error(val msg: String) : MatchResult()
}

@Singleton
class RealtimeDuelRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val queueRef = firestore.collection("matchmaking_queue")
    private val roomsRef = firestore.collection("duel_rooms")
    private val questionsRef = firestore.collection("questions")

    suspend fun joinQueue(entry: QueueEntry) {
        Log.d("DuelRepo", "Joining Queue: ${entry.uid}")
        queueRef.document(entry.uid).set(entry.toMap()).await()
    }

    suspend fun leaveQueue(uid: String) {
        if (uid.isEmpty()) return
        Log.d("DuelRepo", "Leaving Queue: $uid")
        try { queueRef.document(uid).delete().await() } catch (_: Exception) {}
    }

    suspend fun tryFindOpponentAndCreateRoom(player: QueueEntry): MatchResult {
        return try {
            // FORCE SERVER SOURCE to ensure we see the other device's presence
            val querySnapshot = queueRef
                .whereEqualTo("status", "waiting")
                .get(Source.SERVER)
                .await()

            val candidates = querySnapshot.documents.filter { it.id != player.uid }
            Log.d("DuelRepo", "Searching for opponents for ${player.uid}. Found ${candidates.size} online.")

            if (candidates.isEmpty()) return MatchResult.StillSearching

            // Select the oldest entry to avoid multiple people jumping on the same target
            val candidate = candidates.minByOrNull { 
                it.getTimestamp("timestamp")?.seconds ?: Long.MAX_VALUE 
            } ?: return MatchResult.StillSearching

            val opponentUid = candidate.id
            val question = fetchRandomQuestion(player.eloRating) ?: getFallbackQuestion()
            val roomId = roomsRef.document().id

            Log.d("DuelRepo", "MATCHMAKING: Attempting to match ${player.uid} with $opponentUid")

            val result = runMatchmakingTransaction(
                player = player,
                opponentUid = opponentUid,
                roomId = roomId,
                question = question,
                opponentData = candidate.data ?: emptyMap()
            )
            
            if (result != null) {
                Log.d("DuelRepo", "MATCH SUCCESS: Created room $roomId")
                MatchResult.Matched(result, isPlayer1 = true)
            } else {
                Log.d("DuelRepo", "MATCH CONFLICT: Transaction failed (likely opponent already matched)")
                MatchResult.StillSearching
            }
        } catch (e: Exception) {
            Log.e("DuelRepo", "Matchmaking Error: ${e.message}")
            MatchResult.StillSearching
        }
    }

    private suspend fun runMatchmakingTransaction(
        player: QueueEntry,
        opponentUid: String,
        roomId: String,
        question: Question,
        opponentData: Map<String, Any>
    ): String? {
        return try {
            firestore.runTransaction { tx ->
                val opDoc = tx.get(queueRef.document(opponentUid))
                val myDoc = tx.get(queueRef.document(player.uid))
                
                // Atomic check: both must still be waiting
                if (opDoc.getString("status") != "waiting") throw Exception("Opponent no longer waiting")
                if (myDoc.getString("status") != "waiting") throw Exception("I am no longer waiting")

                val oppName = opponentData["displayName"] as? String ?: "Warrior"
                val oppElo = (opponentData["eloRating"] as? Long)?.toInt() ?: 1200
                val oppEmoji = opponentData["rankEmoji"] as? String ?: "🥉"

                val roomData = buildRoomMap(
                    p1U = player.uid, p1N = player.displayName, p1E = player.eloRating, p1Em = player.rankEmoji,
                    p2U = opponentUid, p2N = oppName, p2E = oppElo, p2Em = oppEmoji,
                    question = question, createdAtMs = System.currentTimeMillis()
                )
                
                tx.set(roomsRef.document(roomId), roomData)
                tx.update(queueRef.document(player.uid), mapOf("status" to "matched", "roomId" to roomId, "isPlayer1" to true))
                tx.update(queueRef.document(opponentUid), mapOf("status" to "matched", "roomId" to roomId, "isPlayer1" to false))
                roomId
            }.await()
        } catch (e: Exception) {
            Log.d("DuelRepo", "Transaction failed: ${e.message}")
            null
        }
    }

    fun listenToQueueDoc(uid: String): Flow<MatchResult> = callbackFlow {
        if (uid.isEmpty()) return@callbackFlow
        val reg = queueRef.document(uid).addSnapshotListener { snap, err ->
            if (err != null) return@addSnapshotListener
            if (snap?.getString("status") == "matched") {
                val rId = snap.getString("roomId") ?: return@addSnapshotListener
                val p1 = snap.getBoolean("isPlayer1") ?: false
                Log.d("DuelRepo", "Queue Listener TRIGGERED: Matched as ${if(p1) "P1" else "P2"}")
                trySend(MatchResult.Matched(rId, p1))
            }
        }
        awaitClose { reg.remove() }
    }

    fun listenToRoom(roomId: String): Flow<DuelRoom?> = callbackFlow {
        val reg = roomsRef.document(roomId).addSnapshotListener { snap, err ->
            if (err != null || snap == null) {
                trySend(null); return@addSnapshotListener
            }
            trySend(parseRoom(roomId, snap.data ?: emptyMap()))
        }
        awaitClose { reg.remove() }
    }

    suspend fun setPlayerReady(roomId: String, isPlayer1: Boolean) {
        Log.d("DuelRepo", "READY TAP: Room=$roomId, isP1=$isPlayer1")
        try {
            firestore.runTransaction { tx ->
                val roomRef = roomsRef.document(roomId)
                val roomDoc = tx.get(roomRef)
                
                val status = roomDoc.getString("status") ?: "waiting"
                if (status != "waiting") {
                    Log.d("DuelRepo", "Ready ignored: status is $status")
                    return@runTransaction
                }

                val fieldPrefix = if (isPlayer1) "player1" else "player2"
                val otherPrefix = if (isPlayer1) "player2" else "player1"
                
                tx.update(roomRef, "$fieldPrefix.isReady", true)
                
                val isOtherReady = roomDoc.getBoolean("$otherPrefix.isReady") ?: false
                if (isOtherReady) {
                    Log.d("DuelRepo", "Both players ready -> STARTING")
                    tx.update(roomRef, mapOf(
                        "status" to "in_progress",
                        "startedAtMs" to System.currentTimeMillis()
                    ))
                } else {
                    Log.d("DuelRepo", "One player ready. Waiting for other.")
                }
            }.await()
        } catch (e: Exception) {
            Log.e("DuelRepo", "setPlayerReady Error: ${e.message}")
        }
    }

    suspend fun submitAnswer(roomId: String, isPlayer1: Boolean, answer: String) {
        val prefix = if (isPlayer1) "player1" else "player2"
        roomsRef.document(roomId).update(mapOf("$prefix.answer" to answer, "$prefix.answeredAtMs" to System.currentTimeMillis())).await()
    }

    suspend fun finalizeResult(roomId: String, room: DuelRoom) {
        try {
            firestore.runTransaction { tx ->
                val snap = tx.get(roomsRef.document(roomId))
                if (snap.getString("status") == "finished") return@runTransaction
                
                val q = room.question
                val p1Answer = snap.get("player1.answer") as? String
                val p2Answer = snap.get("player2.answer") as? String
                val p1Time = snap.getLong("player1.answeredAtMs") ?: Long.MAX_VALUE
                val p2Time = snap.getLong("player2.answeredAtMs") ?: Long.MAX_VALUE
                
                val p1Correct = p1Answer == q.correctAnswer
                val p2Correct = p2Answer == q.correctAnswer
                
                val winnerId = when {
                    p1Correct && !p2Correct -> room.player1.uid
                    p2Correct && !p1Correct -> room.player2.uid
                    p1Correct && p2Correct -> if (p1Time <= p2Time) room.player1.uid else room.player2.uid
                    else -> if (p1Answer != null || p2Answer != null) "draw" else "none"
                }
                
                val (d1, d2) = calculateElo(room.player1.eloRating, room.player2.eloRating, winnerId, room.player1.uid)
                tx.update(roomsRef.document(roomId), mapOf(
                    "status" to "finished", 
                    "winnerId" to winnerId, 
                    "eloChange1" to d1, 
                    "eloChange2" to d2
                ))
            }.await()
        } catch (_: Exception) {}
    }

    suspend fun applyEloChange(uid: String, delta: Int, currentElo: Int) {
        if (uid.isEmpty()) return
        val newElo = (currentElo + delta).coerceAtLeast(100)
        firestore.collection("users").document(uid).set(mapOf("eloRating" to newElo), SetOptions.merge()).await()
    }

    private suspend fun fetchRandomQuestion(playerElo: Int): Question? {
        return try {
            val snaps = questionsRef.get().await().documents
            if (snaps.isEmpty()) return null
            val doc = snaps.random()
            Question(
                id = doc.id, topic = doc.getString("topic") ?: "DSA", title = doc.getString("title") ?: "Duel",
                description = doc.getString("description") ?: "", optionA = doc.getString("optionA") ?: "A",
                optionB = doc.getString("optionB") ?: "B", optionC = doc.getString("optionC") ?: "C",
                optionD = doc.getString("optionD") ?: "D", correctAnswer = doc.getString("correctAnswer") ?: "A",
                timeLimitSeconds = doc.getLong("timeLimitSeconds")?.toInt() ?: 60, explanation = doc.getString("explanation") ?: "",
                difficulty = doc.getString("difficulty") ?: "MEDIUM"
            )
        } catch (_: Exception) { null }
    }

    fun getFallbackQuestion() = Question(id="f", topic="DSA", title="Complexity", description="Binary search complexity?", optionA="O(1)", optionB="O(n)", optionC="O(log n)", optionD="O(n^2)", correctAnswer="C")

    @Suppress("UNCHECKED_CAST")
    private fun parseRoom(roomId: String, data: Map<String, Any>): DuelRoom {
        fun parsePlayer(key: String): RoomPlayer {
            val m = data[key] as? Map<String, Any> ?: return RoomPlayer()
            return RoomPlayer(
                uid = m["uid"] as? String ?: "", 
                displayName = m["displayName"] as? String ?: "Warrior", 
                eloRating = (m["eloRating"] as? Long)?.toInt() ?: 1200, 
                rankEmoji = m["rankEmoji"] as? String ?: "🥉", 
                answer = m["answer"] as? String, 
                answeredAtMs = m["answeredAtMs"] as? Long, 
                isReady = m["isReady"] as? Boolean ?: false
            )
        }
        val qMap = data["question"] as? Map<String, Any> ?: emptyMap()
        val question = Question(id = qMap["id"] as? String ?: "", topic = qMap["topic"] as? String ?: "", title = qMap["title"] as? String ?: "", description = qMap["description"] as? String ?: "", optionA = qMap["optionA"] as? String ?: "", optionB = qMap["optionB"] as? String ?: "", optionC = qMap["optionC"] as? String ?: "", optionD = qMap["optionD"] as? String ?: "", correctAnswer = qMap["correctAnswer"] as? String ?: "A", timeLimitSeconds = (qMap["timeLimitSeconds"] as? Long)?.toInt() ?: 60, explanation = qMap["explanation"] as? String ?: "", difficulty = qMap["difficulty"] as? String ?: "MEDIUM")
        return DuelRoom(roomId = roomId, player1 = parsePlayer("player1"), player2 = parsePlayer("player2"), question = question, status = data["status"] as? String ?: "waiting", startedAtMs = data["startedAtMs"] as? Long, winnerId = data["winnerId"] as? String, eloChange1 = (data["eloChange1"] as? Long)?.toInt() ?: 0, eloChange2 = (data["eloChange2"] as? Long)?.toInt() ?: 0, createdAtMs = (data["createdAtMs"] as? Long) ?: 0L)
    }

    private fun buildRoomMap(p1U: String, p1N: String, p1E: Int, p1Em: String, p2U: String, p2N: String, p2E: Int, p2Em: String, question: Question, createdAtMs: Long): Map<String, Any?> = mapOf("player1" to mapOf("uid" to p1U, "displayName" to p1N, "eloRating" to p1E, "rankEmoji" to p1Em, "answer" to null, "answeredAtMs" to null, "isReady" to false), "player2" to mapOf("uid" to p2U, "displayName" to p2N, "eloRating" to p2E, "rankEmoji" to p2Em, "answer" to null, "answeredAtMs" to null, "isReady" to false), "question" to mapOf("id" to question.id, "topic" to question.topic, "title" to question.title, "description" to question.description, "optionA" to question.optionA, "optionB" to question.optionB, "optionC" to question.optionC, "optionD" to question.optionD, "correctAnswer" to question.correctAnswer, "timeLimitSeconds" to question.timeLimitSeconds, "explanation" to question.explanation, "difficulty" to question.difficulty), "status" to "waiting", "startedAtMs" to null, "winnerId" to null, "eloChange1" to 0, "eloChange2" to 0, "createdAtMs" to createdAtMs)

    private fun calculateElo(elo1: Int, elo2: Int, winnerId: String, uid1: String): Pair<Int, Int> {
        val expected1 = 1.0 / (1.0 + 10.0.pow((elo2 - elo1) / 400.0))
        val actual1 = when (winnerId) { uid1 -> 1.0; "draw" -> 0.5; else -> 0.0 }
        val delta1 = (32 * (actual1 - expected1)).toInt()
        val delta2 = (32 * ((1.0 - actual1) - (1.0 - expected1))).toInt()
        return Pair(delta1, delta2)
    }
}
