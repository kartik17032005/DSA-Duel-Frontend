package com.example.dsa_duel.screens.auth.duel

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.models.DuelState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MatchmakingScreen(state: DuelState.FindingMatch, onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLine by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "scan"
    )
    val pulse by infiniteTransition.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(Modifier.fillMaxSize().background(BgDark), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(100.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    repeat(3) { ring ->
                        drawCircle(
                            Cyan.copy(alpha = 0.15f + ring * 0.05f),
                            radius = (20f + ring * 20f) * pulse,
                            style = Stroke(1.5f)
                        )
                    }
                    drawArc(
                        Brush.sweepGradient(listOf(Color.Transparent, Cyan.copy(0.6f)), center = Offset(cx, cy)),
                        startAngle = scanLine * 360f - 90f, sweepAngle = 90f, useCenter = true
                    )
                }
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = Purple,
                    strokeWidth = 2.dp,
                    trackColor = Purple.copy(0.15f)
                )
            }
            Spacer(Modifier.height(36.dp))
            Text(
                "SCANNING FOR OPPONENT",
                fontFamily = FontFamily.Monospace,
                color = Cyan,
                fontSize = 13.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(0.6f + 0.4f * pulse)
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MY DEVICE ID: ${state.myUid.takeLast(8).ifEmpty { "PENDING" }}", color = White.copy(0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    Text("STATUS: ${state.statusMessage}", color = Cyan.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(Modifier.height(48.dp))
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, Red.copy(0.6f)),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
            ) {
                Text("ABORT SEARCH", fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun VsScreen(state: DuelState.MatchFound, onDuelStart: () -> Unit) {
    val orbitalAngle by rememberInfiniteTransition(label = "orb").animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "oa"
    )

    Box(Modifier.fillMaxSize().background(BgDark), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ARENA PREPARED", color = Cyan, letterSpacing = 8.sp, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(60.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FighterCard(state.myName, state.myElo, "👤", Cyan, orbitalAngle, state.isMeReady)
                Text("VS", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Gold, modifier = Modifier.padding(horizontal = 10.dp))
                FighterCard(
                    state.opponent.name,
                    state.opponent.eloRating,
                    state.opponent.rankEmoji,
                    Red,
                    -orbitalAngle,
                    state.isOpponentReady || state.isBot
                )
            }
            Spacer(Modifier.height(80.dp))
            
            val isReady = state.isMeReady
            GlowButton(
                text = if (isReady) "WAITING FOR OPPONENT..." else "⚔  READY TO FIGHT",
                color = if (isReady) GrayMuted else Purple,
                enabled = !isReady,
                onClick = onDuelStart
            )
        }
    }
}

@Composable
fun FighterCard(name: String, elo: Int, emoji: String, color: Color, angle: Float, isReady: Boolean) {
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color.copy(0.2f),
                    size.width * 0.45f,
                    style = Stroke(1.dp.toPx(), pathEffect = dashEffect)
                )
                val rad = (angle * PI / 180.0)
                drawCircle(
                    if (isReady) Green else color,
                    4.dp.toPx(),
                    Offset(
                        size.width / 2 + cos(rad).toFloat() * size.width * 0.45f,
                        size.height / 2 + sin(rad).toFloat() * size.width * 0.45f
                    )
                )
            }
            Box(
                Modifier.size(60.dp).background(color.copy(0.1f), CircleShape)
                    .border(2.dp, if (isReady) Green else color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 30.sp)
            }
            
            if (isReady) {
                Surface(
                    color = Green,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 8.dp)
                ) {
                    Text("READY", color = White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(name.uppercase().take(10), color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("$elo ELO", color = if (isReady) Green else color, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
