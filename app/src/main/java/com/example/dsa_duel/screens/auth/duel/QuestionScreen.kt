package com.example.dsa_duel.screens.auth.duel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.models.DuelState
import kotlinx.coroutines.delay
import kotlin.math.*
import androidx.compose.ui.graphics.toArgb

// ═══════════════════════════════════════════════════════════════════
//  SHARED PALETTE EXTENSIONS
// ══════════════════════════════

private val Q_DSA_CHARS = listOf(
    "O(n)", "{}", "[]", "→", "∑", "λ", "log",
    "n²", "O(1)", "dp[", "BFS", "DFS", "++",
    "null", "swap", "<<", "&&", "heap", "map"
)

private val Q_BgDark   = Color(0xFF080716)
private val Q_CardBg   = Color(0xFF111022)
private val Q_CardBg2  = Color(0xFF0D0C1F)
private val Q_Cyan     = Color(0xFF00F2FF)
private val Q_CyanDim  = Color(0xFF00A3AD)
private val Q_Purple   = Color(0xFFBC00FF)
private val Q_Gold     = Color(0xFFFFD700)
private val Q_Red      = Color(0xFFFF3D00)
private val Q_White    = Color(0xFFFFFFFF)
private val Q_GrayMuted = Color(0xFF888796)

// ═══════════════════════════════════════════════════════════════════
//  UTILITIES - Optimized Path creation
// ═══════════════════════════════════════════════════════════════════
private fun createUnitHexPath(): Path = Path().apply {
    for (i in 0..5) {
        val a = (60.0 * i - 30.0) * PI / 180.0
        val x = cos(a).toFloat()
        val y = sin(a).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

// ═══════════════════════════════════════════════════════════════════
//  QUESTION SCREEN
// ═══════════════════════════════════════════════════════════════════
@Composable
fun QuestionScreen(state: DuelState.InProgress, onAnswer: (String) -> Unit) {

    val options = listOf(
        "A" to state.question.optionA,
        "B" to state.question.optionB,
        "C" to state.question.optionC,
        "D" to state.question.optionD
    )

    val timeLimit = state.question.timeLimitSeconds.coerceAtLeast(1)
    val timeRatio = (state.timeLeftSeconds.toFloat() / timeLimit.toFloat()).coerceIn(0f, 1f)
    val isLow = state.timeLeftSeconds < 10
    val isCritical = state.timeLeftSeconds < 5
    val botLocked = state.timeLeftSeconds <= state.opponentSolvesAtSecond

    // ── Infinite transitions ──────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "q")
    val codeRain by inf.animateFloat(0f, 1000f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "cr")
    val hexShimmer by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(4200), RepeatMode.Reverse), label = "hs")
    val bgBreath by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "bb")
    val timerPulse by inf.animateFloat(1f, 1.06f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "tp")
    val edgeDanger by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "ed")
    val botGlow by inf.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "bg")
    val scanLine by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "sl")
    val orbAngle by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "oa")

    val unitHex = remember { createUnitHexPath() }
    val rainPaint = remember {
        android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    val timerColor = when {
        isCritical -> Q_Red
        isLow -> Q_Gold
        else -> Q_Cyan
    }

    Box(Modifier
        .fillMaxSize()
        .background(Q_BgDark)) {

        // ── BG Layer 1: Hex grid + code rain ─────────────────────
        Canvas(Modifier.fillMaxSize()) {
            val hSz = 30.dp.toPx()
            val hH = hSz * sqrt(3f) / 2f

            // Hex grid (Optimization: reuse unit path with transform)
            repeat((size.height / hH).toInt() + 2) { row ->
                repeat((size.width / (hSz * 1.5f)).toInt() + 2) { col ->
                    val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                    val hcy = row * hH
                    val sh = (sin(hexShimmer * PI * 2 + col * 0.4 + row * 0.3) * 0.5 + 0.5).toFloat()

                    withTransform({
                        translate(hcx, hcy)
                        scale(hSz * 0.42f, hSz * 0.42f, Offset.Zero)
                    }) {
                        drawPath(
                            unitHex,
                            Q_Cyan.copy(0.02f + sh * 0.015f),
                            style = Stroke(0.6f / (hSz * 0.42f))
                        )
                    }
                }
            }

            // Scan line
            val sl = scanLine * size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Q_Cyan.copy(0.06f), Q_Cyan.copy(0.03f), Color.Transparent),
                    startY = sl - 50f, endY = sl + 50f
                ),
                topLeft = Offset(0f, sl - 50f), size = Size(size.width, 100f)
            )

            // Code rain
            val nc = drawContext.canvas.nativeCanvas
            val colsCount = 14
            val cw = size.width / colsCount
            rainPaint.textSize = 8.dp.toPx()
            repeat(colsCount) { col ->
                repeat(8) { row ->
                    val ch = Q_DSA_CHARS[(col * 5 + row * 3) % Q_DSA_CHARS.size]
                    val by = ((row / 8f) + (codeRain / 1000f)) % 1f
                    val al = (1f - by) * 0.09f
                    if (al > 0.01f) {
                        rainPaint.color = when (col % 3) {
                            0 -> Q_Cyan.copy(al).toArgb()
                            1 -> Q_Purple.copy(al).toArgb()
                            else -> Green.copy(al * 0.6f).toArgb()
                        }
                        nc.drawText(ch, col * cw + cw / 2f, by * size.height, rainPaint)
                    }
                }
            }
        }

        // ── BG Layer 2: Danger edge vignette ─────
        if (isLow) {
            val dangerAlpha = (if (isCritical) 0.25f else 0.13f) * edgeDanger
            Box(Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color.Transparent, Q_Red.copy(dangerAlpha)),
                            center = center, radius = size.maxDimension * 0.75f
                        )
                    )
                })
        }

        // ── BG Layer 3: Ambient purple-cyan wash ──────────────────
        Box(Modifier
            .fillMaxSize()
            .alpha(0.07f + bgBreath * 0.04f)
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(Q_Purple.copy(0.5f), Color.Transparent), center = Offset.Zero, radius = 600f))
            })
        Box(Modifier
            .fillMaxSize()
            .alpha(0.07f + bgBreath * 0.03f)
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(Q_CyanDim.copy(0.4f), Color.Transparent), center = Offset(size.width, size.height), radius = 700f))
            })

        // ── Main scrollable content ───────────────────────────────
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(14.dp))

            TopBar(
                timeLeft = state.timeLeftSeconds,
                timeRatio = timeRatio,
                timerColor = timerColor,
                timerPulse = if (isLow) timerPulse else 1f,
                botLocked = botLocked,
                botGlow = botGlow,
                orbAngle = orbAngle
            )

            Spacer(Modifier.height(20.dp))
            QuestionCard(state = state, hexShimmer = hexShimmer)
            Spacer(Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                options.forEachIndexed { index, (key, value) ->
                    OptionCard(
                        index = index,
                        optionKey = key,
                        text = value,
                        isSelected = state.selectedOption == key,
                        isDisabled = state.selectedOption != null && state.selectedOption != key,
                        accent = when (index) { 0 -> Q_Cyan; 1 -> Q_Purple; 2 -> Q_Gold; else -> Q_Red },
                        onClick = { if (state.selectedOption == null) onAnswer(key) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(
    timeLeft: Int, timeRatio: Float,
    timerColor: Color, timerPulse: Float,
    botLocked: Boolean, botGlow: Float, orbAngle: Float
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.scale(timerPulse), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(72.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width * 0.46f
                drawCircle(timerColor.copy(0.1f), r, Offset(cx, cy), style = Stroke(3.dp.toPx()))
                drawArc(
                    color = timerColor.copy(0.9f),
                    startAngle = -90f,
                    sweepAngle = -360f * (1f - timeRatio),
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(timerColor.copy(0.12f), r * 1.15f, Offset(cx, cy), style = Stroke(1f))
                val tipAngle = ((-90.0 - 360.0 * (1.0 - timeRatio)) * PI / 180.0)
                drawCircle(timerColor.copy(0.95f), 4.dp.toPx(), Offset(cx + cos(tipAngle).toFloat() * r, cy + sin(tipAngle).toFloat() * r))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${timeLeft}s", color = timerColor, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text("TIME", color = timerColor.copy(0.5f), fontSize = 7.sp, letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            val unitHex = remember { createUnitHexPath() }
            Canvas(Modifier.fillMaxSize()) {
                val r = size.width * 0.44f
                rotate(orbAngle) {
                    withTransform({ scale(r, r, center) }) {
                        drawPath(unitHex, Q_Purple.copy(0.25f), style = Stroke(1.dp.toPx() / r, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f))))
                    }
                }
                val dotRad = (orbAngle * PI / 180.0)
                drawCircle(Q_Gold.copy(0.9f), 3.dp.toPx(), Offset(center.x + cos(dotRad).toFloat() * r, center.y + sin(dotRad).toFloat() * r))
            }
            Text("⚡", fontSize = 22.sp)
        }

        Box(Modifier
            .drawBehind {
                if (botLocked) {
                    val radius = 12.dp.toPx()
                    drawRoundRect(Q_Red.copy(botGlow * 0.12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
                    drawRoundRect(Q_Red.copy(botGlow * 0.4f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius), style = Stroke(1.dp.toPx()))
                }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.CenterEnd) {
            Column(horizontalAlignment = Alignment.End) {
                Text("BOT STATUS", color = Q_GrayMuted, fontSize = 8.sp, letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(3.dp))
                if (botLocked) {
                    Surface(color = Q_Red.copy(0.15f), shape = RoundedCornerShape(4.dp), border = BorderStroke(0.8.dp, Q_Red.copy(0.6f * botGlow))) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(Modifier.size(5.dp).background(Q_Red.copy(botGlow), CircleShape))
                            Text("LOCKED IN", color = Q_Red, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("SOLVING", color = Q_Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        SolvingDots()
                    }
                }
            }
        }
    }
}

@Composable
private fun SolvingDots() {
    val inf = rememberInfiniteTransition(label = "dots")
    val phase by inf.animateFloat(0f, 3f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "ph")
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val a = ((phase - i).coerceIn(0f, 1f)).let { if (it < 0.5f) it * 2 else (1f - it) * 2 }
            Box(Modifier.size(4.dp).alpha(0.3f + a * 0.7f).background(Q_Gold, CircleShape))
        }
    }
}

@Composable
private fun QuestionCard(state: DuelState.InProgress, hexShimmer: Float) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(120); visible = true }
    val unitHex = remember { createUnitHexPath() }

    AnimatedVisibility(visible = visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400, easing = FastOutSlowInEasing)) { -30 }) {
        Box {
            Box(Modifier.matchParentSize().blur(14.dp).background(Q_Purple.copy(0.25f), RoundedCornerShape(20.dp)))
            Surface(modifier = Modifier.fillMaxWidth(), color = Q_CardBg2, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Q_Cyan.copy(0.4f), Q_Purple.copy(0.6f), Q_Purple.copy(0.3f), Q_Cyan.copy(0.2f))))) {
                Box {
                    Canvas(Modifier.matchParentSize()) {
                        val hSz = 22.dp.toPx()
                        val hH = hSz * sqrt(3f) / 2f
                        repeat((size.height / hH).toInt() + 1) { row ->
                            repeat((size.width / (hSz * 1.5f)).toInt() + 1) { col ->
                                val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                                val hcy = row * hH
                                val sh = (sin(hexShimmer * PI * 2 + col * 0.6 + row * 0.5) * 0.5 + 0.5).toFloat()
                                withTransform({
                                    translate(hcx, hcy)
                                    scale(hSz * 0.4f, hSz * 0.4f, Offset.Zero)
                                }) {
                                    drawPath(unitHex, Q_Purple.copy(0.02f + sh * 0.02f), style = Stroke(0.5f / (hSz * 0.4f)))
                                }
                            }
                        }
                        drawRect(Brush.verticalGradient(listOf(Q_Purple.copy(0.08f), Color.Transparent), startY = 0f, endY = (size.height * 0.45f).coerceAtLeast(1f)))
                    }
                    Column(Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Q_Purple.copy(0.18f), shape = RoundedCornerShape(5.dp), border = BorderStroke(0.8.dp, Q_Purple.copy(0.5f))) {
                                Text(state.question.topic.uppercase(), color = Q_Purple, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(3) { i ->
                                    Box(Modifier.size(width = 10.dp, height = 3.dp).background(when(i) { 0 -> Green; 1 -> Q_Gold; else -> Q_Red }.copy(if (i <= 1) 0.8f else 0.3f), RoundedCornerShape(1.5.dp)))
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(text = state.question.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Q_White, lineHeight = 27.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.fillMaxWidth().height(0.8.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Q_Cyan.copy(0.3f), Q_Purple.copy(0.4f), Color.Transparent))))
                        Spacer(Modifier.height(10.dp))
                        Text(text = state.question.description, fontSize = 13.5.sp, color = Q_White.copy(0.62f), lineHeight = 22.sp)
                        if (state.question.description.length > 100) {
                            Spacer(Modifier.height(12.dp))
                            Surface(color = Color(0xFF050310), shape = RoundedCornerShape(8.dp), border = BorderStroke(0.8.dp, Q_Cyan.copy(0.2f))) {
                                Row(Modifier.padding(12.dp)) {
                                    Column { repeat(4) { n -> Text("${n + 1}", color = Q_GrayMuted.copy(0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp) } }
                                    Spacer(Modifier.width(12.dp))
                                    Box(Modifier.width(0.8.dp).height(72.dp).background(Q_Cyan.copy(0.1f)))
                                    Spacer(Modifier.width(12.dp))
                                    Text("// Think carefully...\nfun solve(input: Array<Int>): Int {\n    // logic\n}", color = Q_Cyan.copy(0.7f), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(index: Int, optionKey: String, text: String, isSelected: Boolean, isDisabled: Boolean, accent: Color, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80L + index * 90L); visible = true }
    val inf = rememberInfiniteTransition(label = "opt$index")
    val borderGlow by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(900 + index * 120), RepeatMode.Reverse), label = "bg")
    val selectedScale by animateFloatAsState(if (isSelected) 1.02f else 1f, spring(dampingRatio = 0.55f, stiffness = 300f), label = "sel")

    AnimatedVisibility(visible = visible, enter = fadeIn(tween(320)) + slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { -40 }) {
        Box(Modifier.scale(selectedScale)) {
            if (isSelected) Box(Modifier.matchParentSize().blur(16.dp).background(accent.copy(0.35f), RoundedCornerShape(16.dp)))
            Surface(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDisabled,
                color = when { isSelected -> accent.copy(0.18f); isDisabled -> Q_CardBg.copy(0.5f); else -> Q_CardBg },
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(if (isSelected) 1.5.dp else 0.8.dp, when {
                    isSelected -> Brush.linearGradient(listOf(accent, accent.copy(0.5f), accent.copy(0.9f)))
                    isDisabled -> SolidColor(Q_White.copy(0.04f))
                    else -> Brush.linearGradient(listOf(accent.copy(0.25f * borderGlow), Q_White.copy(0.06f), accent.copy(0.15f * borderGlow)))
                })
            ) {
                Box {
                    if (isSelected) Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(accent.copy(0.12f), Color.Transparent))))
                    if (isSelected) {
                        Canvas(Modifier.matchParentSize()) {
                            drawLine(accent.copy(0.85f), Offset.Zero, Offset(size.width * 0.35f, 0f), 2.dp.toPx(), StrokeCap.Round)
                            drawLine(accent.copy(0.5f), Offset(size.width * 0.65f, size.height), Offset(size.width, size.height), 2.dp.toPx(), StrokeCap.Round)
                            repeat(6) { i -> val x = size.width * (0.55f + i * 0.08f); val y = size.height * 0.5f + (if (i % 2 == 0) -6f else 6f); drawCircle(accent.copy(0.3f), 1.5.dp.toPx(), Offset(x, y)) }
                        }
                    }
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).drawBehind { if (isSelected) drawCircle(accent.copy(0.3f), size.maxDimension * 0.6f) }.background(if (isSelected) accent.copy(0.25f) else accent.copy(0.08f), RoundedCornerShape(8.dp)).border(BorderStroke(if (isSelected) 1.5.dp else 0.8.dp, if (isSelected) accent.copy(0.8f) else accent.copy(0.25f)), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Text(optionKey, color = if (isSelected) accent else accent.copy(0.7f), fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(text = text, color = when { isSelected -> Q_White; isDisabled -> Q_White.copy(0.3f); else -> Q_White.copy(0.82f) }, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                        AnimatedVisibility(visible = isSelected, enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn()) {
                            Spacer(Modifier.width(10.dp))
                            Box(Modifier.size(20.dp).background(accent.copy(0.3f), CircleShape).border(BorderStroke(1.dp, accent.copy(0.7f)), CircleShape), contentAlignment = Alignment.Center) { Text("✓", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                        }
                    }
                }
            }
        }
    }
}
