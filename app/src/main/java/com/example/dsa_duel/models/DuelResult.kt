package com.example.dsa_duel.models


data class DuelResult(
    val topic: String,
    val difficulty: String,
    val playerWon: Boolean,
    val eloChange: Int,
    val botName: String,
    val timeTakenSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    // How long ago this duel happened — for display
    // Example: "2h ago", "Yesterday", "3 days ago"
    val timeAgoLabel: String
        get() {
            val diff  = System.currentTimeMillis() - timestamp
            val min   = 60_000L
            val hour  = 3_600_000L
            val day   = 86_400_000L
            return when {
                diff < min        -> "just now"
                diff < hour       -> "${diff / min}m ago"
                diff < day        -> "${diff / hour}h ago"
                diff < 2 * day    -> "Yesterday"
                diff < 7 * day    -> "${diff / day} days ago"
                else              -> "Over a week ago"
            }
        }

    // ELO change as display string with sign
    // Example: "+24" or "-19"
    val eloChangeLabel: String
        get() = if (eloChange >= 0) "+$eloChange" else "$eloChange"
}

/*
 * ── HOW DuelState IS USED IN DuelViewModel ────────────────────────
 *
 * private val _state = MutableStateFlow<DuelState>(DuelState.Idle)
 * val state: StateFlow<DuelState> = _state.asStateFlow()
 *
 * fun findDuel() {
 *     _state.value = DuelState.FindingMatch(playerElo = 1500)
 *     // ... do work ...
 *     _state.value = DuelState.MatchFound(bot, question, gain, loss)
 * }
 *
 * ── HOW DuelState IS USED IN DuelScreen (Compose) ─────────────────
 *
 * val state by viewModel.state.collectAsState()
 *
 * when (state) {
 *     is DuelState.Idle         → DuelSetupContent(...)
 *     is DuelState.FindingMatch → MatchmakingContent(...)
 *     is DuelState.MatchFound   → VsScreenContent(...)
 *     is DuelState.InProgress   → QuestionContent(...)
 *     is DuelState.Result       → ResultContent(...)
 *     is DuelState.Error        → ErrorContent(...)
 * }
 *
 * The `when` block is EXHAUSTIVE — Kotlin forces you to handle
 * every sealed class subtype. If you add a new state and forget
 * to handle it in the UI, the code won't compile. This is the
 * safety guarantee that makes sealed classes so powerful.
 *
 * ── STATE TRANSITION DIAGRAM ─────────────────────────────────────
 *
 *                    ┌─────────────────────────────┐
 *                    ↓                             │
 *   ┌──────┐   tap Find   ┌──────────────┐         │
 *   │ Idle │ ──────────→  │ FindingMatch │         │
 *   └──────┘              └──────────────┘         │
 *      ↑                         │ 2s delay        │
 *      │                         ↓                 │
 *      │                  ┌────────────┐           │
 *      │                  │MatchFound  │           │
 *      │                  └────────────┘           │
 *      │                         │ 3s VS screen    │
 *      │                         ↓                 │
 *      │                  ┌────────────┐           │
 *      │                  │ InProgress │           │
 *      │                  └────────────┘           │
 *      │                         │ answer/timeout  │
 *      │                         ↓                 │
 *      │   Play Again     ┌────────────┐           │
 *      └──────────────────│   Result   │           │
 *                         └────────────┘           │
 *                                                  │
 *   Any state → Error → tap Retry ──────────────── ┘
 */