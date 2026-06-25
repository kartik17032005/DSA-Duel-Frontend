package com.example.dsa_duel.screens.auth.duel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.models.DuelState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

private val R_BgDark = Color(0xFF020205)
private val R_CardBg = Color(0xFF0C0C18)
private val R_CardBg2 = Color(0xFF080612)
private val R_Purple = Color(0xFF8B5CF6)
private val R_Cyan = Color(0xFF06B6D4)
private val R_Gold = Color(0xFFF59E0B)
private val R_Red = Color(0xFFEF4444)
private val R_Green = Color(0xFF22C55E)
private val R_White = Color(0xFFFFFFFF)
private val R_Gray = Color(0xFF6B7280)

private fun createUnitHexPath(): Path = Path().apply {
    for (i in 0..5) {
        val a = (60.0 * i - 30.0) * PI / 180.0
        val x = cos(a).toFloat()
        val y = sin(a).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private val R_DSA_CHARS = listOf(
    "O(n)", "{}", "[]", "→", "∑", "λ", "log", "n²",
    "O(1)", "dp[", "BFS", "DFS", "++", "null", "swap", "heap"
)

@Composable
fun ResultScreen(
    state: DuelState.Result,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val isWin = state.playerWon
    val isTimeout = state.wasTimeout
    val accentColor = when {
        isWin -> R_Green
        isTimeout -> R_Gold
        else -> R_Red
    }

    val bannerScale = remember { Animatable(0f) }
    val bannerAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentY = remember { Animatable(40f) }
    val shockwave1 = remember { Animatable(0f) }
    val shockwave2 = remember { Animatable(0f) }
    val particleP = remember { Animatable(0f) }
    val screenFlash = remember { Animatable(0f) }

    val animatedElo by animateIntAsState(
        targetValue = state.newElo,
        animationSpec = tween(1800, 600, FastOutSlowInEasing),
        label = "elo"
    )
    val animatedEloChange by animateIntAsState(
        targetValue = state.eloChange,
        animationSpec = tween(1400, 700, FastOutSlowInEasing),
        label = "eloChg"
    )

    val inf = rememberInfiniteTransition(label = "res")
    val codeRain by inf.animateFloat(
        0f,
        1000f,
        infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "cr"
    )
    val hexShimmer by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(3800), RepeatMode.Reverse),
        label = "hs"
    )
    val bgBreath by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label = "bb"
    )
    val orbAngle by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "oa"
    )
    val bannerGlow by inf.animateFloat(
        0.6f,
        1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "bg"
    )
    val scanLine by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "sl"
    )

    val sparksCount = 28
    val sparkAngles =
        remember { List(sparksCount) { it * (360f / sparksCount) + (-15..15).random().toFloat() } }
    val sparkSpeeds = remember { List(sparksCount) { 0.5f + (Math.random() * 0.8f).toFloat() } }
    val sparkColors = remember {
        List(sparksCount) {
            if (isWin) listOf(
                R_Green,
                R_Gold,
                R_Cyan,
                R_White
            )[it % 4] else listOf(R_Red, R_Gold, R_Purple, R_White)[it % 4]
        }
    }
    val sparkSizesList = remember { List(sparksCount) { 2f + (Math.random() * 4f).toFloat() } }

    val unitHex = remember { createUnitHexPath() }
    val rainPaint = remember {
        android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    LaunchedEffect(Unit) {
        launch { screenFlash.animateTo(1f, tween(60)); screenFlash.animateTo(0f, tween(500)) }
        launch { shockwave1.animateTo(1f, tween(1000, easing = LinearOutSlowInEasing)) }
        launch { delay(100); shockwave2.animateTo(1f, tween(900, easing = LinearOutSlowInEasing)) }
        launch { particleP.animateTo(1f, tween(1400, easing = FastOutLinearInEasing)) }
        bannerAlpha.animateTo(1f, tween(120))
        bannerScale.animateTo(1.3f, tween(250, easing = FastOutLinearInEasing))
        bannerScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 260f)
        )
        delay(500)
        launch { contentAlpha.animateTo(1f, tween(500)) }
        contentY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 160f))
    }

    Box(Modifier
        .fillMaxSize()
        .background(R_BgDark)) {
        Canvas(Modifier.fillMaxSize()) {
            rainPaint.textSize = 8.dp.toPx()
            val hSz = 30.dp.toPx()
            val hH = hSz * sqrt(3f) / 2f
            repeat((size.height / hH).toInt() + 2) { row ->
                repeat((size.width / (hSz * 1.5f)).toInt() + 2) { col ->
                    val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                    val hcy = row * hH
                    val sh =
                        (sin(hexShimmer * PI * 2 + col * 0.4 + row * 0.3) * 0.5 + 0.5).toFloat()
                    withTransform({
                        translate(hcx, hcy)
                        scale(hSz * 0.42f, hSz * 0.42f, Offset.Zero)
                    }) {
                        drawPath(
                            unitHex,
                            accentColor.copy(0.018f + sh * 0.012f),
                            style = Stroke(0.6f / (hSz * 0.42f))
                        )
                    }
                }
            }
            val sl = scanLine * size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        accentColor.copy(0.06f),
                        Color.Transparent
                    ), startY = sl - 45f, endY = sl + 45f
                ),
                topLeft = Offset(0f, sl - 45f), size = Size(size.width, 90f)
            )
            val nc = drawContext.canvas.nativeCanvas
            val colCount = 14
            val cw = size.width / colCount
            repeat(colCount) { col ->
                repeat(8) { row ->
                    val ch = R_DSA_CHARS[(col * 5 + row * 3) % R_DSA_CHARS.size]
                    val by = ((row / 8f) + (codeRain / 1000f)) % 1f
                    val al = (1f - by) * 0.08f
                    if (al > 0.01f) {
                        rainPaint.color = (when (col % 3) {
                            0 -> accentColor.copy(al)
                            1 -> R_Purple.copy(al)
                            else -> R_Cyan.copy(al * 0.6f)
                        }).toArgb()
                        nc.drawText(ch, col * cw + cw / 2f, by * size.height, rainPaint)
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.08f + bgBreath * 0.05f)
                .background(
                    Brush.radialGradient(
                        listOf(accentColor.copy(0.5f), Color.Transparent),
                        center = Offset.Zero,
                        radius = 700f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.05f + bgBreath * 0.03f)
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            listOf(
                                R_Purple.copy(0.4f),
                                Color.Transparent
                            ), center = Offset(size.width, size.height), radius = 600f
                        )
                    )
                })

        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.22f
            listOf(
                shockwave1.value to (accentColor to 3.5.dp.toPx()),
                shockwave2.value to (R_Cyan to 1.5.dp.toPx())
            ).forEach { (prog, cc) ->
                if (prog in 0.001f..0.999f) {
                    drawCircle(
                        cc.first.copy((1f - prog) * 0.65f),
                        prog * size.maxDimension * 0.85f,
                        Offset(cx, cy),
                        style = Stroke(cc.second * (1f - prog * 0.6f))
                    )
                }
            }
            if (particleP.value > 0f) {
                repeat(sparksCount) { i ->
                    val p = (particleP.value * sparkSpeeds[i]).coerceIn(0f, 1f)
                    if (p > 0f) {
                        val rad = sparkAngles[i] * PI / 180.0
                        val ease = 1f - (1f - p).pow(2f)
                        val dist = ease * 110.dp.toPx()
                        val sx = cx + cos(rad).toFloat() * dist
                        val sy = cy + sin(rad).toFloat() * dist
                        drawCircle(
                            sparkColors[i].copy((1f - p.pow(0.55f)).coerceIn(0f, 1f)),
                            sparkSizesList[i] * (1.1f - p * 0.7f),
                            Offset(sx, sy)
                        )
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Box(
                Modifier
                    .scale(bannerScale.value)
                    .alpha(bannerAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(180.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.width * 0.46f
                    rotate(orbAngle) {
                        withTransform({ translate(cx, cy); scale(r, r, Offset.Zero) }) {
                            drawPath(
                                unitHex,
                                accentColor.copy(0.18f),
                                style = Stroke(
                                    1.dp.toPx() / r,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                )
                            )
                        }
                    }
                    drawCircle(accentColor.copy(bannerGlow * 0.18f), r * 0.65f, Offset(cx, cy))
                    val r1 = orbAngle * PI / 180.0
                    drawCircle(
                        accentColor.copy(0.95f),
                        4.dp.toPx(),
                        Offset(cx + cos(r1).toFloat() * r, cy + sin(r1).toFloat() * r)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when {
                            isWin -> "⚔"; isTimeout -> "⏱"; else -> "🛡"
                        },
                        fontSize = 44.sp,
                        modifier = Modifier.drawBehind {
                            drawCircle(
                                accentColor.copy(0.25f),
                                size.maxDimension * 0.85f
                            )
                        })
                    Spacer(Modifier.height(10.dp))
                    Text(
                        when {
                            isWin -> "VICTORY"; isTimeout -> "TIMEOUT"; else -> "DEFEAT"
                        },
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 6.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "VS ${state.opponent.name.uppercase()}",
                        color = R_Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Column(
                Modifier
                    .offset(y = contentY.value.dp)
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EloStatCard(
                        "ELO CHANGE",
                        if (state.eloChange >= 0) "+$animatedEloChange" else "$animatedEloChange",
                        if (state.eloChange >= 0) R_Green else R_Red,
                        Modifier.weight(1f),
                        hexShimmer,
                        if (state.eloChange >= 0) R_Green else R_Red,
                        unitHex
                    )
                    EloStatCard(
                        "NEW RATING",
                        "$animatedElo",
                        R_White,
                        Modifier.weight(1f),
                        hexShimmer,
                        R_Cyan,
                        unitHex
                    )
                }
                Spacer(Modifier.height(24.dp))
                SectionHeader("⚔ BATTLE SUMMARY")
                Spacer(Modifier.height(10.dp))
                listOf(
                    "A" to state.question.optionA,
                    "B" to state.question.optionB,
                    "C" to state.question.optionC,
                    "D" to state.question.optionD
                ).forEachIndexed { idx, (k, v) ->
                    AnswerCard(idx, k, v, k == state.correctAnswer, k == state.playerAnswer)
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(20.dp))
                SectionHeader("💡 BATTLE ANALYSIS")
                Spacer(Modifier.height(10.dp))
                AnalysisCard(state.explanation)
                Spacer(Modifier.height(32.dp))
                ResultGlowButton("⚔ REMATCH", R_Purple, onPlayAgain)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, R_Gray.copy(0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = R_Gray)
                ) {
                    Text(
                        "QUIT ARENA",
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
        if (screenFlash.value > 0.01f) Box(
            Modifier
                .fillMaxSize()
                .background(R_White.copy(screenFlash.value))
        )
    }
}

@Composable
private fun EloStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier,
    hexShimmer: Float,
    accentColor: Color,
    unitHex: Path
) {
    val inf = rememberInfiniteTransition(label = "esc")
    val borderGlow by inf.animateFloat(
        0.4f,
        0.9f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "bg"
    )
    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .blur(8.dp)
                .background(accentColor.copy(0.15f), RoundedCornerShape(16.dp))
        )
        Surface(
            color = R_CardBg2,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, accentColor.copy(0.4f * borderGlow))
        ) {
            Box {
                Canvas(Modifier.matchParentSize()) {
                    val hSz = 18.dp.toPx()
                    val hH = hSz * sqrt(3f) / 2f
                    repeat((size.height / hH).toInt() + 1) { row ->
                        repeat((size.width / (hSz * 1.5f)).toInt() + 1) { col ->
                            val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                            val hcy = row * hH
                            val sh =
                                (sin(hexShimmer * PI * 2 + col * 0.7 + row * 0.5) * 0.5 + 0.5).toFloat()
                            withTransform({
                                translate(hcx, hcy); scale(
                                hSz * 0.4f,
                                hSz * 0.4f,
                                Offset.Zero
                            )
                            }) {
                                drawPath(
                                    unitHex,
                                    accentColor.copy(0.015f + sh * 0.015f),
                                    style = Stroke(0.5f / (hSz * 0.4f))
                                )
                            }
                        }
                    }
                }
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label,
                        color = R_Gray,
                        fontSize = 8.sp,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        value,
                        color = color,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.7.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, R_Gray.copy(0.3f))))
        )
        Text(
            text,
            color = R_Gray,
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Box(
            Modifier
                .weight(1f)
                .height(0.7.dp)
                .background(Brush.horizontalGradient(listOf(R_Gray.copy(0.3f), Color.Transparent)))
        )
    }
}

@Composable
private fun AnswerCard(
    index: Int,
    optionKey: String,
    text: String,
    isCorrect: Boolean,
    isPlayerChoice: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(700L + index * 110L); visible = true }
    val accent = if (isCorrect) R_Green else if (isPlayerChoice) R_Red else R_Gray
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            tween(
                350,
                easing = FastOutSlowInEasing
            )
        ) { -36 }) {
        Surface(
            color = if (isCorrect) R_Green.copy(0.08f) else if (isPlayerChoice) R_Red.copy(0.08f) else R_CardBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                if (isCorrect || isPlayerChoice) 1.2.dp else 0.7.dp,
                accent.copy(0.4f)
            )
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(accent.copy(0.14f), RoundedCornerShape(8.dp))
                        .border(0.6.dp, accent.copy(0.25f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        optionKey,
                        color = accent,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text,
                    color = if (isCorrect || isPlayerChoice) R_White else R_Gray.copy(0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isCorrect || isPlayerChoice) Text(
                    if (isCorrect) "✓" else "✗",
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun AnalysisCard(explanation: String) {
    Surface(
        color = R_CardBg2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, R_Gold.copy(0.3f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "BATTLE ANALYSIS",
                color = R_Gold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(10.dp))
            Text(explanation, color = R_White.copy(0.72f), fontSize = 13.5.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ResultGlowButton(label: String, color: Color, onClick: () -> Unit) {
    val glow by rememberInfiniteTransition().animateFloat(
        0.5f,
        1f,
        infiniteRepeatable(tween(950), RepeatMode.Reverse)
    )
    Box(Modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color.copy(0.15f * glow)),
            border = BorderStroke(1.5.dp, color.copy(0.8f))
        ) {
            Text(
                label,
                color = R_White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )
        }
    }
}
