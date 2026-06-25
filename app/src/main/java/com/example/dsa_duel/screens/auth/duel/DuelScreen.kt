package com.example.dsa_duel.screens.auth.duel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.models.DuelState
import com.example.dsa_duel.viewModels.DuelViewModel

// Global Palette used across the duel module
val BgDark = Color(0xFF020205)
val CardBg = Color(0xFF0B0D17)
val CardBg2 = Color(0xFF11142B)
val Purple = Color(0xFF8B5CF6)
val Cyan = Color(0xFF06B6D4)
val CyanDim = Color(0xFF0891B2)
val Red = Color(0xFFEF4444)
val Green = Color(0xFF10B981)
val White = Color(0xFFFFFFFF)
val GrayMuted = Color(0xFF6B7280)
val Gold = Color(0xFFF59E0B)

@Composable
fun DuelScreen(
    onNavigateHome: () -> Unit,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().background(BgDark)) {
        when (val s = state) {
            is DuelState.Idle -> DuelSetupScreen(onFindDuel = { viewModel.findDuel() })
            is DuelState.FindingMatch -> MatchmakingScreen(state = s, onCancel = { viewModel.cancelMatchmaking() })
            is DuelState.MatchFound -> VsScreen(state = s, onDuelStart = { viewModel.startDuel() })
            is DuelState.InProgress -> QuestionScreen(state = s, onAnswer = { viewModel.submitAnswer(it) })
            is DuelState.Result -> ResultScreen(
                state = s,
                onPlayAgain = { viewModel.resetDuel() },
                onHome = { viewModel.resetDuel(); onNavigateHome() }
            )
            is DuelState.Error -> ErrorScreen(s.message, onRetry = { viewModel.resetDuel() })
        }
    }
}

@Composable
fun DuelSetupScreen(onFindDuel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DSA DUEL ARENA", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(8.dp))
        Text("RANKED MULTIPLAYER", color = Cyan, fontSize = 12.sp, letterSpacing = 4.sp)
        Spacer(Modifier.height(60.dp))
        GlowButton("⚔  FIND OPPONENT", Purple, onClick = onFindDuel)
    }
}

@Composable
fun GlowButton(text: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = GrayMuted.copy(0.3f)
        )
    ) {
        Text(text, color = if (enabled) White else White.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}

@Composable
fun ErrorScreen(msg: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🚨", fontSize = 48.sp)
        Text("DUEL ERROR", color = Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(16.dp))
        Surface(color = Red.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
            Text(msg, color = White.copy(0.8f), modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(32.dp))
        GlowButton("RETRY", Red, onClick = onRetry)
    }
}
