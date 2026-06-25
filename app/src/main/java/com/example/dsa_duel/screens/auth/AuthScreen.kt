package com.example.dsa_duel.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsa_duel.R
import com.example.dsa_duel.components.AuthCard
import com.example.dsa_duel.components.LoginRegisterToggle
import com.example.dsa_duel.navigation.Routes
import com.example.dsa_duel.viewModels.AuthViewModel

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var isLogin by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsState()

    // Navigation logic: when user is not null, go to Home
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            navController.navigate(Routes.HOME) {
                // Clear backstack so user can't go back to Auth screen
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundAnim")

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Scanline"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
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
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0A24), Color(0xFF030308), Color(0xFF070514))
                    )
                )

                val gridSize = 35.dp.toPx()
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(
                        color = Color(0xFF1A1A2E).copy(alpha = 0.4f),
                        start = Offset(x * gridSize, 0f),
                        end = Offset(x * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(
                        color = Color(0xFF1A1A2E).copy(alpha = 0.4f),
                        start = Offset(0f, y * gridSize),
                        end = Offset(size.width, y * gridSize),
                        strokeWidth = 1f
                    )
                }

                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, neonCyan.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    start = Offset(0f, size.height * scanlineY),
                    end = Offset(size.width, size.height * scanlineY),
                    strokeWidth = 3.dp.toPx()
                )

                val cornerSize = 35.dp.toPx()
                val pad = 20.dp.toPx()
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(pad, pad), Offset(pad + cornerSize, pad), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(pad, pad), Offset(pad, pad + cornerSize), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(size.width - pad, size.height - pad), Offset(size.width - pad - cornerSize, size.height - pad), 3f)
                drawLine(activeHUDColor.copy(alpha = glowAlpha), Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - cornerSize), 3f)
            }
    ) {
        Text(
            text = buildAnnotatedString {
                append("MODE: [${if(isLogin) "DUEL_READY" else "INIT_CHALLENGE"}]\n")
                append("SECTOR: [C-137]\n")
                append("STATUS: ONLINE")
            },
            modifier = Modifier.padding(24.dp).align(Alignment.TopStart),
            color = activeHUDColor.copy(0.4f),
            fontSize = 9.sp,
            fontFamily = FontFamily(Font(R.font.jetbrains_mono_medium)),
            lineHeight = 12.sp
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Spacer(modifier = Modifier.height(24.dp))
            LoginRegisterToggle(isLogin = isLogin, onToggle = { isLogin = it })
            AuthCard(isLogin = isLogin, onToggle = { isLogin = it }, viewModel = viewModel)
        }
    }
}
