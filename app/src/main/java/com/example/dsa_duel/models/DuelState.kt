package com.example.dsa_duel.models

import com.example.dsa_duel.data.QuestionEntity

/**
 * FILE: models/DuelState.kt
 */
sealed class DuelState {

    data object Idle : DuelState()

    data class FindingMatch(
        val playerElo: Int,
        val queueSize: Int = 0,
        val myUid: String = "",
        val statusMessage: String = "CONNECTING..."
    ) : DuelState()

    data class MatchFound(
        val myElo: Int,
        val myName: String,
        val opponent: BotProfile,
        val question: QuestionEntity,
        val potentialGain: Int,
        val potentialLoss: Int,
        val isMeReady: Boolean = false,
        val isOpponentReady: Boolean = false,
        val isBot: Boolean = false
    ) : DuelState()

    data class InProgress(
        val question: QuestionEntity,
        val opponent: BotProfile,
        val timeLeftSeconds: Int,
        val opponentSolvesAtSecond: Int,
        val selectedOption: String?,
        val playerElo: Int,
        val opponentElo: Int
    ) : DuelState()

    data class Result(
        val playerWon: Boolean,
        val playerAnswer: String,
        val correctAnswer: String,
        val explanation: String,
        val eloChange: Int,
        val newElo: Int,
        val opponent: BotProfile,
        val timeTakenSeconds: Int,
        val wasTimeout: Boolean,
        val question: QuestionEntity
    ) : DuelState()

    data class Error(
        val message: String
    ) : DuelState()
}
