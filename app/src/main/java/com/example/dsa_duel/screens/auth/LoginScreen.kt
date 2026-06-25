package com.example.dsa_duel.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.R
import com.example.dsa_duel.components.AuthCard
import com.example.dsa_duel.components.LoginRegisterToggle
import com.example.dsa_duel.data.LoginResponse
import com.example.dsa_duel.viewModels.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (LoginResponse) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.user) {
        authState.user?.let {
            onLoginSuccess(it)
        }
    }

    AuthBackground(isLogin = true) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection(isLogin = true)
            Spacer(modifier = Modifier.height(24.dp))
            LoginRegisterToggle(isLogin = true, onToggle = { if (!it) onNavigateToRegister() })
            AuthCard(
                isLogin = true, 
                onToggle = { if (!it) onNavigateToRegister() },
                onForgotPasswordClick = onNavigateToForgotPassword,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun AuthBackground(
    isLogin: Boolean, 
    onBack: (() -> Unit)? = null, // 🔥 Added onBack callback
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundAnim")
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "Scanline"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "GlowPulse"
    )

    val neonCyan = Color(0xFF00C2FF)
    val neonPurple = Color(0xFF8F67FF)
    val activeHUDColor = if (isLogin) neonPurple else neonCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030308))
            .drawBehind {
                drawRect(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0F0A24), Color(0xFF030308), Color(0xFF070514))))
                val gridSize = 35.dp.toPx()
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(Color(0xFF1A1A2E).copy(alpha = 0.4f), Offset(x * gridSize, 0f), Offset(x * gridSize, size.height), 1f)
                }
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(Color(0xFF1A1A2E).copy(alpha = 0.4f), Offset(0f, y * gridSize), Offset(size.width, y * gridSize), 1f)
                }
                drawLine(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, neonCyan.copy(alpha = 0.1f), Color.Transparent)), start = Offset(0f, size.height * scanlineY), end = Offset(size.width, size.height * scanlineY), strokeWidth = 3.dp.toPx())
                val cornerSize = 35.dp.toPx(); val pad = 20.dp.toPx()
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(pad, pad), Offset(pad + cornerSize, pad), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(pad, pad), Offset(pad, pad + cornerSize), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(size.width - pad, size.height - pad), Offset(size.width - pad - cornerSize, size.height - pad), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - cornerSize), 3f)
            }
    ) {
        // 🔥 Back Button
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = activeHUDColor
                )
            }
        }

        Text(
            text = buildAnnotatedString {
                append("MODE: [${if(isLogin) "DUEL_READY" else "INIT_CHALLENGE"}]\n")
                append("SECTOR: [C-137]\n")
                append("STATUS: ONLINE")
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, start = if (onBack != null) 56.dp else 24.dp)
                .align(Alignment.TopStart),
            color = activeHUDColor.copy(0.4f),
            fontSize = 9.sp,
            fontFamily = FontFamily(Font(R.font.jetbrains_mono_medium)),
            lineHeight = 12.sp
        )
        content()
    }
}

@Composable
fun HeaderSection(isLogin: Boolean) {
    val neonCyan = Color(0xFF00C2FF)
    val neonPurple = Color(0xFF8F67FF)
    val activeHUDColor = if (isLogin) neonPurple else neonCyan

    Text(text = "⚔️ BATTLE LOBBY SEASON 1", color = activeHUDColor, fontFamily = FontFamily(Font(R.font.jetbrains_mono_medium)), fontSize = 11.sp, letterSpacing = 2.sp)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White)) { append("DSA") }
            withStyle(style = SpanStyle(brush = Brush.linearGradient(colors = listOf(neonPurple, neonCyan)))) { append("DUEL") }
        },
        fontSize = 44.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
        letterSpacing = (-1).sp
    )
    Text(text = "CODE • COMPETE • CONQUER", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily(Font(R.font.jetbrains_mono_medium)), fontSize = 10.sp, letterSpacing = 5.sp)
    Text(text = "Compete in real-time DSA battles. Climb the leaderboard.", color = Color.White.copy(alpha = 0.4f), fontFamily = FontFamily(Font(R.font.rajdhani_medium)), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp).padding(horizontal = 40.dp))
}
