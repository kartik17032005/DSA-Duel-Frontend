package com.example.dsa_duel.screens.auth

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.R
import com.example.dsa_duel.viewModels.AnalyticsUiState
import com.example.dsa_duel.viewModels.AnalyticsViewModel
import com.example.dsa_duel.viewModels.EloPoint
import com.example.dsa_duel.viewModels.RecentDuel
import com.example.dsa_duel.viewModels.TopicStat
import kotlinx.coroutines.delay
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════
//  PALETTE & FONTS
// ═══════════════════════════════════════════════════════════════
private val A_BgDark = Color(0xFF020205)
private val A_CardBg = Color(0xFF0C0C18)
private val A_CardBg2 = Color(0xFF080612)
private val A_Purple = Color(0xFF8B5CF6)
private val A_Cyan = Color(0xFF06B6D4)
private val A_CyanDim = Color(0xFF0891B2)
private val A_Gold = Color(0xFFFFB800)
private val A_Red = Color(0xFFEF4444)
private val A_Green = Color(0xFF22C55E)
private val A_White = Color(0xFFFFFFFF)
private val A_Gray = Color(0xFF6B7280)
private val A_Ember = Color(0xFFFF6B35)

private val A_Mono = FontFamily(Font(R.font.jetbrains_mono_medium))
private val A_Bold = FontFamily(Font(R.font.rajdhani_bold))
private val A_Med = FontFamily(Font(R.font.rajdhani_medium))

private val TOPIC_COLORS = mapOf(
    "Arrays" to Color(0xFF8F67FF),
    "LinkedList" to Color(0xFF00C2FF),
    "Strings" to Color(0xFF00C2FF),
    "Stacks" to Color(0xFFFF8C00),
    "Trees" to Color(0xFFFFB800),
    "Sorting" to Color(0xFFFF6B6B),
    "BinarySearch" to Color(0xFF4CAF50),
    "Graphs" to Color(0xFFE91E63),
    "DP" to Color(0xFFE91E63)
)

private fun topicColor(t: String) = TOPIC_COLORS[t] ?: A_Purple

private fun aHex(cx: Float, cy: Float, r: Float): Path = Path().apply {
    for (i in 0..5) {
        val a = Math.toRadians(60.0 * i - 30.0)
        if (i == 0) moveTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
        else lineTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
    }
    close()
}

private val A_DSA =
    listOf("O(n)", "{}", "[]", "→", "∑", "λ", "log", "n²", "dp[", "BFS", "DFS", "++", "heap")

// ═══════════════════════════════════════════════════════════════
//  ANALYTICS SCREEN
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val inf = rememberInfiniteTransition(label = "an")
    val codeRain by inf.animateFloat(
        0f,
        1000f,
        infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "cr"
    )
    val hexShimmer by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "hs"
    )
    val bgBreath by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "bb"
    )
    val scanLine by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "sl"
    )

    Box(Modifier
        .fillMaxSize()
        .background(A_BgDark)) {

        // ── BG: hex grid + code rain ─────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            val hSz = 30.dp.toPx();
            val hH = hSz * sqrt(3f) / 2f
            repeat((size.height / hH).toInt() + 2) { row ->
                repeat((size.width / (hSz * 1.5f)).toInt() + 2) { col ->
                    val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                    val sh =
                        (sin(hexShimmer * PI * 2 + col * 0.4 + row * 0.3) * 0.5 + 0.5).toFloat()
                    drawPath(
                        aHex(hcx, row * hH, hSz * 0.42f),
                        A_Cyan.copy(0.018f + sh * 0.012f), style = Stroke(0.55f)
                    )
                }
            }
            val sl = scanLine * size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, A_Cyan.copy(0.05f), Color.Transparent),
                    startY = sl - 40f, endY = sl + 40f
                ),
                topLeft = Offset(0f, sl - 40f), size = Size(size.width, 80f)
            )
            val nc = drawContext.canvas.nativeCanvas
            val cols = 12;
            val cw = size.width / cols
            repeat(cols) { col ->
                repeat(7) { row ->
                    val ch = A_DSA[(col * 4 + row * 3) % A_DSA.size]
                    val by = ((row / 7f) + (codeRain / 1000f)) % 1f
                    val al = (1f - by) * 0.07f
                    if (al > 0.01f) {
                        nc.drawText(ch, col * cw, by * size.height, Paint().apply {
                            textSize = 8.dp.toPx()
                            color = when (col % 3) {
                                0 -> A_Cyan.copy(al).toArgb()
                                1 -> A_Purple.copy(al).toArgb()
                                else -> A_Green.copy(al * 0.5f).toArgb()
                            }
                            typeface = Typeface.MONOSPACE
                        })
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.05f + bgBreath * 0.03f)
                .background(
                    Brush.radialGradient(
                        listOf(A_Purple.copy(0.5f), Color.Transparent),
                        center = Offset(0f, 0f), radius = 650f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.04f)
                .background(
                    Brush.radialGradient(
                        listOf(A_CyanDim.copy(0.35f), Color.Transparent),
                        center = Offset.Infinite, radius = 600f
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(4.dp, 16.dp)
                                    .background(
                                        Brush.verticalGradient(listOf(A_Cyan, A_Purple)),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                "BATTLE ANALYTICS", fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp,
                                fontFamily = A_Mono, color = A_White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(A_Purple.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, A_Purple.copy(0.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, "Back",
                                    tint = A_White, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(A_Cyan.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, A_Cyan.copy(0.25f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Refresh, "Refresh",
                                    tint = A_Cyan, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent, titleContentColor = A_White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            if (state.isLoading) {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = A_Purple,
                        strokeWidth = 2.dp,
                        trackColor = A_Purple.copy(0.1f)
                    )
                }
                return@Scaffold
            }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(6.dp)) }

                // ── Summary header cards ──────────────────────────────
                item { SummaryRow(state) }

                // ── ELO Progression ───────────────────────────────────
                item {
                    AnalyticsSectionHeader(
                        "📈  ELO PROGRESSION",
                        "Last ${state.eloHistory.size} Duels"
                    )
                }
                item {
                    if (state.eloHistory.size < 2) {
                        EmptyChartPlaceholder("Play more duels to see your ELO trend")
                    } else {
                        EloLineChart(state.eloHistory)
                    }
                }

                // ── Topic Performance ─────────────────────────────────
                item { AnalyticsSectionHeader("⚔  TOPIC PERFORMANCE", "Win Rate per Topic") }
                item {
                    if (state.topicStats.isEmpty()) {
                        EmptyChartPlaceholder("Complete duels to see topic stats")
                    } else {
                        TopicBarChart(state.topicStats)
                    }
                }

                // ── Skill Radar ───────────────────────────────────────
                item { AnalyticsSectionHeader("🕸  SKILL RADAR", "Mastery Profile") }
                item {
                    if (state.topicStats.size < 3) {
                        EmptyChartPlaceholder("Unlock 3+ topics to see your radar")
                    } else {
                        RadarChart(state.topicStats.take(6))
                    }
                }

                // ── Recent Form ───────────────────────────────────────
                item { AnalyticsSectionHeader("🔥  RECENT FORM", "Last 5 Duels") }
                item { RecentFormRow(state) }

                // ── Insights card ─────────────────────────────────────
                if (state.bestTopic.isNotBlank() || state.worstTopic.isNotBlank()) {
                    item { AnalyticsSectionHeader("💡  INSIGHTS", "AI Recommendations") }
                    item { InsightsCard(state) }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SUMMARY ROW
// ═══════════════════════════════════════════════════════════════
@Composable
private fun SummaryRow(state: AnalyticsUiState) {
    val winRate = if (state.totalDuels > 0) (state.totalWins * 100 / state.totalDuels) else 0
    val cards = listOf(
        Triple("WIN RATE", "$winRate%", A_Cyan),
        Triple("PEAK ELO", "${state.peakElo}", A_Gold),
        Triple(
            "7D CHANGE", "${if (state.eloChange7d >= 0) "+" else ""}${state.eloChange7d}",
            if (state.eloChange7d >= 0) A_Green else A_Red
        ),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        cards.forEach { (label, value, color) ->
            var vis by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(80); vis = true }
            AnimatedVisibility(
                vis, enter = fadeIn(tween(350)) + scaleIn(tween(380), 0.9f),
                modifier = Modifier.weight(1f)
            ) {
                Box {
                    Box(
                        Modifier
                            .matchParentSize()
                            .blur(10.dp)
                            .background(color.copy(0.18f), RoundedCornerShape(14.dp))
                    )
                    Surface(
                        color = A_CardBg2, shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            0.8.dp, Brush.linearGradient(
                                listOf(color.copy(0.5f), color.copy(0.15f)),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
                        )
                    ) {
                        Box {
                            Canvas(Modifier.matchParentSize()) {
                                drawRect(
                                    Brush.verticalGradient(
                                        listOf(
                                            color.copy(0.06f),
                                            Color.Transparent
                                        ), 0f, size.height * 0.5f
                                    )
                                )
                            }
                            Column(
                                Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    label,
                                    color = A_Gray,
                                    fontSize = 7.5.sp,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = A_Mono,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    value, color = color, fontSize = 20.sp,
                                    fontFamily = A_Bold, fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SECTION HEADER
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AnalyticsSectionHeader(title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title, color = A_White, fontSize = 13.sp, fontFamily = A_Bold,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
            )
            Text(subtitle, color = A_Gray, fontSize = 9.sp, fontFamily = A_Mono)
        }
        Box(
            Modifier
                .height(0.7.dp)
                .width(60.dp)
                .background(Brush.horizontalGradient(listOf(A_Gray.copy(0.3f), Color.Transparent)))
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  EMPTY STATE
// ═══════════════════════════════════════════════════════════════
@Composable
private fun EmptyChartPlaceholder(msg: String) {
    Surface(
        color = A_CardBg2, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.7.dp, A_White.copy(0.06f))
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(120.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("◈", color = A_Gray.copy(0.5f), fontSize = 22.sp)
                Text(
                    msg, color = A_Gray, fontSize = 11.sp, fontFamily = A_Mono,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  ELO LINE CHART
// ═══════════════════════════════════════════════════════════════
@Composable
private fun EloLineChart(history: List<EloPoint>) {
    // Animate the draw progress 0→1 on first appearance
    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(200)
        drawProgress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
    }

    val minElo = (history.minOf { it.elo } - 30).toFloat()
    val maxElo = (history.maxOf { it.elo } + 30).toFloat()
    val eloRange = (maxElo - minElo).coerceAtLeast(1f)

    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(12.dp)
                .background(A_Purple.copy(0.15f), RoundedCornerShape(18.dp))
        )
        Surface(
            color = A_CardBg2, shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp, Brush.linearGradient(
                    listOf(A_Purple.copy(0.4f), A_Cyan.copy(0.2f)),
                    start = Offset.Zero, end = Offset.Infinite
                )
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                // Y-axis labels row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${maxElo.toInt()}", color = A_Gray, fontSize = 8.sp, fontFamily = A_Mono)
                    Text(
                        "ELO", color = A_Purple, fontSize = 9.sp, fontFamily = A_Mono,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${minElo.toInt()}", color = A_Gray, fontSize = 8.sp, fontFamily = A_Mono)
                }
                Spacer(Modifier.height(6.dp))

                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val padH = 12.dp.toPx();
                    val padV = 8.dp.toPx()
                    val w = size.width - padH * 2
                    val h = size.height - padV * 2
                    val n = history.size

                    fun xOf(i: Int) = padH + i * w / (n - 1).coerceAtLeast(1)
                    fun yOf(elo: Int) = padV + h * (1f - (elo - minElo) / eloRange)

                    // ── Grid lines ────────────────────────────────────
                    repeat(5) { i ->
                        val y = padV + h * i / 4f
                        drawLine(A_White.copy(0.05f), Offset(padH, y), Offset(padH + w, y), 0.7f)
                    }

                    // ── Compute visible points up to drawProgress ─────
                    val visibleCount = ((n - 1) * drawProgress.value).toInt() + 1
                    val partialFrac = ((n - 1) * drawProgress.value) - (visibleCount - 1)

                    if (visibleCount < 1) return@Canvas

                    // ── Gradient fill path ────────────────────────────
                    val fillPath = Path()
                    fillPath.moveTo(xOf(0), yOf(history[0].elo))
                    for (i in 1 until visibleCount) {
                        val cx1 = (xOf(i - 1) + xOf(i)) / 2f
                        fillPath.cubicTo(
                            cx1,
                            yOf(history[i - 1].elo),
                            cx1,
                            yOf(history[i].elo),
                            xOf(i),
                            yOf(history[i].elo)
                        )
                    }
                    // Partial last segment
                    if (visibleCount < n && partialFrac > 0f) {
                        val prevX = xOf(visibleCount - 1);
                        val prevY = yOf(history[visibleCount - 1].elo)
                        val nextX = xOf(visibleCount);
                        val nextY = yOf(history[visibleCount].elo)
                        val cx1 = (prevX + nextX) / 2f
                        val tx = prevX + (nextX - prevX) * partialFrac
                        val ty = prevY + (nextY - prevY) * partialFrac
                        fillPath.cubicTo(cx1, prevY, cx1, ty, tx, ty)
                    }
                    val lastX =
                        if (visibleCount < n) xOf(visibleCount - 1) + (xOf(visibleCount) - xOf(
                            visibleCount - 1
                        )) * partialFrac
                        else xOf(n - 1)
                    val lastY = size.height - padV
                    fillPath.lineTo(lastX, padV + h)
                    fillPath.lineTo(xOf(0), padV + h)
                    fillPath.close()

                    drawPath(
                        fillPath, Brush.verticalGradient(
                            listOf(A_Purple.copy(0.35f), Color.Transparent),
                            startY = padV, endY = padV + h
                        )
                    )

                    // ── Line path ─────────────────────────────────────
                    val linePath = Path()
                    linePath.moveTo(xOf(0), yOf(history[0].elo))
                    for (i in 1 until visibleCount) {
                        val cx1 = (xOf(i - 1) + xOf(i)) / 2f
                        linePath.cubicTo(
                            cx1,
                            yOf(history[i - 1].elo),
                            cx1,
                            yOf(history[i].elo),
                            xOf(i),
                            yOf(history[i].elo)
                        )
                    }
                    if (visibleCount < n && partialFrac > 0f) {
                        val prevX = xOf(visibleCount - 1);
                        val prevY = yOf(history[visibleCount - 1].elo)
                        val nextX = xOf(visibleCount);
                        val nextY = yOf(history[visibleCount].elo)
                        val cx1 = (prevX + nextX) / 2f
                        val tx = prevX + (nextX - prevX) * partialFrac
                        val ty = prevY + (nextY - prevY) * partialFrac
                        linePath.cubicTo(cx1, prevY, cx1, ty, tx, ty)
                    }
                    // Glow
                    drawPath(
                        linePath,
                        A_Purple.copy(0.25f),
                        style = Stroke(6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        linePath,
                        A_Cyan.copy(0.15f),
                        style = Stroke(10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Main line
                    drawPath(
                        linePath, Brush.linearGradient(
                            listOf(A_Purple, A_Cyan),
                            start = Offset(padH, 0f), end = Offset(padH + w, 0f)
                        ),
                        style = Stroke(
                            2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // ── Data points ───────────────────────────────────
                    for (i in 0 until visibleCount) {
                        val px = xOf(i);
                        val py = yOf(history[i].elo)
                        val isWin = history[i].won
                        val dotColor = if (isWin) A_Green else A_Red
                        drawCircle(dotColor.copy(0.3f), 7.dp.toPx(), Offset(px, py))
                        drawCircle(dotColor, 4.dp.toPx(), Offset(px, py))
                        drawCircle(A_White.copy(0.8f), 1.8.dp.toPx(), Offset(px, py))
                    }
                }

                Spacer(Modifier.height(8.dp))
                // X-axis labels
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    history.forEachIndexed { i, pt ->
                        if (i == 0 || i == history.size - 1 || history.size <= 6 || i % (history.size / 4) == 0) {
                            Text(
                                "#${pt.index}",
                                color = A_Gray,
                                fontSize = 7.sp,
                                fontFamily = A_Mono
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(A_Green, "WIN")
                    LegendDot(A_Red, "LOSS")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Text(label, color = A_Gray, fontSize = 8.sp, fontFamily = A_Mono)
    }
}

// ═══════════════════════════════════════════════════════════════
//  TOPIC BAR CHART  (horizontal bars)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun TopicBarChart(topics: List<TopicStat>) {
    val barProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(200)
        barProgress.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
    }

    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(12.dp)
                .background(A_Cyan.copy(0.12f), RoundedCornerShape(18.dp))
        )
        Surface(
            color = A_CardBg2, shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp, Brush.linearGradient(
                    listOf(A_Cyan.copy(0.35f), A_Purple.copy(0.2f)),
                    start = Offset.Zero, end = Offset.Infinite
                )
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                topics.forEach { topic ->
                    val color = topicColor(topic.topic)
                    val winPct = (topic.winRate * 100).toInt()
                    val targetFill = topic.winRate * barProgress.value

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Topic name
                        Text(
                            topic.topic, color = A_White, fontSize = 11.sp,
                            fontFamily = A_Bold, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.width(90.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        // Bar track + fill
                        Box(
                            Modifier
                                .weight(1f)
                                .height(18.dp)
                                .background(A_White.copy(0.05f), RoundedCornerShape(9.dp))
                                .border(
                                    BorderStroke(0.5.dp, color.copy(0.15f)),
                                    RoundedCornerShape(9.dp)
                                )
                        ) {
                            // Fill bar
                            if (targetFill > 0f) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(targetFill)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(color.copy(0.7f), color, color.copy(0.85f))
                                            ),
                                            RoundedCornerShape(9.dp)
                                        )
                                )
                                // Shine on bar
                                Box(
                                    Modifier
                                        .fillMaxHeight(0.4f)
                                        .fillMaxWidth(targetFill)
                                        .align(Alignment.TopStart)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(A_White.copy(0.15f), Color.Transparent)
                                            ),
                                            RoundedCornerShape(
                                                topStart = 9.dp,
                                                topEnd = 4.dp,
                                                bottomStart = 0.dp,
                                                bottomEnd = 0.dp
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Win rate text
                        Text(
                            "$winPct%", color = color, fontSize = 10.sp,
                            fontFamily = A_Mono, fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp), textAlign = TextAlign.End
                        )
                    }

                    // Attempts count
                    Text(
                        "${topic.wins}W / ${topic.total - topic.wins}L  (${topic.total} duels)",
                        color = A_Gray, fontSize = 8.sp, fontFamily = A_Mono,
                        modifier = Modifier.padding(start = 98.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  RADAR / SPIDER CHART
// ═══════════════════════════════════════════════════════════════
@Composable
private fun RadarChart(topics: List<TopicStat>) {
    val fillProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        fillProgress.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
    }

    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(14.dp)
                .background(A_Purple.copy(0.15f), RoundedCornerShape(18.dp))
        )
        Surface(
            color = A_CardBg2, shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp, Brush.linearGradient(
                    listOf(A_Purple.copy(0.4f), A_Cyan.copy(0.2f), A_Purple.copy(0.3f)),
                    start = Offset.Zero, end = Offset.Infinite
                )
            )
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier
                    .fillMaxWidth()
                    .height(240.dp)) {
                    val cx = size.width / 2f;
                    val cy = size.height / 2f
                    val n = topics.size; if (n < 3) return@Canvas
                    val maxR = minOf(cx, cy) * 0.78f
                    val rings = 5

                    // ── Grid rings ────────────────────────────────────
                    repeat(rings) { ring ->
                        val r = maxR * (ring + 1) / rings
                        val ringPath = Path()
                        for (i in 0..n) {
                            val angle = Math.toRadians((360.0 * i / n) - 90.0)
                            val x = cx + r * cos(angle).toFloat()
                            val y = cy + r * sin(angle).toFloat()
                            if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                        }
                        ringPath.close()
                        drawPath(ringPath, A_White.copy(0.06f), style = Stroke(0.7f))
                    }

                    // ── Axis lines ────────────────────────────────────
                    for (i in 0 until n) {
                        val angle = Math.toRadians((360.0 * i / n) - 90.0)
                        drawLine(
                            A_White.copy(0.1f),
                            Offset(cx, cy),
                            Offset(
                                cx + maxR * cos(angle).toFloat(),
                                cy + maxR * sin(angle).toFloat()
                            ),
                            0.7f
                        )
                    }

                    // ── Filled polygon (animated) ─────────────────────
                    val filledPath = Path()
                    topics.forEachIndexed { i, topic ->
                        val angle = Math.toRadians((360.0 * i / n) - 90.0)
                        val r = maxR * topic.mastery * fillProgress.value
                        val x = cx + r * cos(angle).toFloat()
                        val y = cy + r * sin(angle).toFloat()
                        if (i == 0) filledPath.moveTo(x, y) else filledPath.lineTo(x, y)
                    }
                    filledPath.close()

                    // Glow fill
                    drawPath(filledPath, A_Purple.copy(0.25f * fillProgress.value))
                    // Gradient fill
                    drawPath(
                        filledPath, Brush.radialGradient(
                            listOf(
                                A_Cyan.copy(0.35f * fillProgress.value),
                                A_Purple.copy(0.2f * fillProgress.value)
                            ),
                            center = Offset(cx, cy), radius = maxR
                        )
                    )
                    // Stroke
                    drawPath(
                        filledPath, Brush.linearGradient(
                            listOf(A_Cyan, A_Purple, A_Cyan),
                            start = Offset(cx - maxR, cy), end = Offset(cx + maxR, cy)
                        ),
                        style = Stroke(2.dp.toPx())
                    )

                    // ── Vertex dots ───────────────────────────────────
                    topics.forEachIndexed { i, topic ->
                        val angle = Math.toRadians((360.0 * i / n) - 90.0)
                        val r = maxR * topic.mastery * fillProgress.value
                        val x = cx + r * cos(angle).toFloat()
                        val y = cy + r * sin(angle).toFloat()
                        val c = topicColor(topic.topic)
                        drawCircle(c.copy(0.4f), 6.dp.toPx(), Offset(x, y))
                        drawCircle(c, 3.5.dp.toPx(), Offset(x, y))
                        drawCircle(A_White.copy(0.9f), 1.5.dp.toPx(), Offset(x, y))
                    }

                    // ── Axis labels (drawn via nativeCanvas) ──────────
                    val nc = drawContext.canvas.nativeCanvas
                    topics.forEachIndexed { i, topic ->
                        val angle = Math.toRadians((360.0 * i / n) - 90.0)
                        val labelR = maxR + 22.dp.toPx()
                        val lx = cx + labelR * cos(angle).toFloat()
                        val ly = cy + labelR * sin(angle).toFloat()
                        val paint = Paint().apply {
                            textSize = 9.dp.toPx()
                            color = topicColor(topic.topic).copy(0.9f).toArgb()
                            textAlign = when {
                                lx < cx - 5 -> Paint.Align.RIGHT
                                lx > cx + 5 -> Paint.Align.LEFT
                                else -> Paint.Align.CENTER
                            }
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        nc.drawText(topic.topic, lx, ly + paint.textSize / 3f, paint)
                        // Mastery % below label
                        paint.textSize = 7.5f.dp.toPx()
                        paint.color = A_Gray.toArgb()
                        nc.drawText(
                            "${(topic.mastery * 100).toInt()}%", lx,
                            ly + paint.textSize * 2.5f, paint
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                // Legend row
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Box(
                            Modifier
                                .size(width = 24.dp, height = 2.dp)
                                .background(Brush.horizontalGradient(listOf(A_Cyan, A_Purple)))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "MASTERY PROFILE", color = A_Gray, fontSize = 8.sp,
                            fontFamily = A_Mono, letterSpacing = 2.sp
                        )
                    })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  RECENT FORM ROW
// ═══════════════════════════════════════════════════════════════
@Composable
private fun RecentFormRow(state: AnalyticsUiState) {
    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(10.dp)
                .background(A_Purple.copy(0.1f), RoundedCornerShape(16.dp))
        )
        Surface(
            color = A_CardBg2, shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.8.dp, A_Purple.copy(0.25f))
        ) {
            Box {
                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(A_Purple.copy(0.05f), Color.Transparent),
                            0f, size.height * 0.4f
                        )
                    )
                }
                Column(Modifier.padding(16.dp)) {
                    if (state.recentDuels.isEmpty()) {
                        // Show placeholder chips
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(5) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .background(A_White.copy(0.04f), RoundedCornerShape(10.dp))
                                        .border(
                                            BorderStroke(0.6.dp, A_White.copy(0.08f)),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("?", color = A_Gray, fontSize = 16.sp, fontFamily = A_Mono)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Play duels to populate your recent form",
                            color = A_Gray,
                            fontSize = 10.sp,
                            fontFamily = A_Mono,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.recentDuels.forEach { duel ->
                                val color = if (duel.won) A_Green else A_Red
                                val eloText =
                                    if (duel.eloChange >= 0) "+${duel.eloChange}" else "${duel.eloChange}"
                                Box(Modifier.weight(1f)) {
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .blur(8.dp)
                                            .background(color.copy(0.2f), RoundedCornerShape(10.dp))
                                    )
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(68.dp)
                                            .background(color.copy(0.1f), RoundedCornerShape(10.dp))
                                            .border(
                                                BorderStroke(0.8.dp, color.copy(0.5f)),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                if (duel.won) "W" else "L",
                                                color = color,
                                                fontSize = 18.sp,
                                                fontFamily = A_Bold,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                duel.topic.take(5), color = A_White.copy(0.7f),
                                                fontSize = 7.5.sp, fontFamily = A_Mono, maxLines = 1
                                            )
                                            Text(
                                                eloText, color = color, fontSize = 8.sp,
                                                fontFamily = A_Mono, fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(0.7.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        A_Gray.copy(0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(Modifier.height(12.dp))

                    // Quick stats strip
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        QuickStat(
                            "AVG TIME",
                            if (state.avgResponseSec > 0) "${state.avgResponseSec.toInt()}s" else "—",
                            A_Cyan
                        )
                        Box(Modifier
                            .width(0.7.dp)
                            .height(32.dp)
                            .background(A_Gray.copy(0.2f)))
                        QuickStat(
                            "ACCURACY",
                            if (state.avgAccuracy > 0) "${(state.avgAccuracy * 100).toInt()}%" else "—",
                            A_Gold
                        )
                        Box(Modifier
                            .width(0.7.dp)
                            .height(32.dp)
                            .background(A_Gray.copy(0.2f)))
                        QuickStat("TOTAL DUELS", "${state.totalDuels}", A_Purple)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value, color = color, fontSize = 16.sp, fontFamily = A_Bold,
            fontWeight = FontWeight.Black
        )
        Text(label, color = A_Gray, fontSize = 7.5.sp, fontFamily = A_Mono, letterSpacing = 1.sp)
    }
}

// ═══════════════════════════════════════════════════════════════
//  INSIGHTS CARD
// ═══════════════════════════════════════════════════════════════
@Composable
private fun InsightsCard(state: AnalyticsUiState) {
    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(12.dp)
                .background(A_Gold.copy(0.15f), RoundedCornerShape(16.dp))
        )
        Surface(
            color = A_CardBg2, shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp, Brush.linearGradient(
                    listOf(A_Gold.copy(0.4f), A_Purple.copy(0.25f)),
                    start = Offset.Zero, end = Offset.Infinite
                )
            )
        ) {
            Box {
                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(A_Gold.copy(0.06f), Color.Transparent),
                            0f, size.height * 0.4f
                        )
                    )
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(A_Gold, CircleShape)
                        )
                        Text(
                            "COMBAT INTELLIGENCE",
                            color = A_Gold,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            fontFamily = A_Mono,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(0.7.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        A_Gold.copy(0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    if (state.bestTopic.isNotBlank()) {
                        InsightRow(
                            "🏆", "STRENGTH",
                            "Your strongest topic is ${state.bestTopic}. Keep dueling here to maintain dominance.",
                            A_Green
                        )
                    }
                    if (state.worstTopic.isNotBlank()) {
                        InsightRow(
                            "⚠", "WEAKNESS",
                            "${state.worstTopic} is holding you back. Focus duels here to climb ELO faster.",
                            A_Red
                        )
                    }
                    if (state.eloChange7d != 0) {
                        InsightRow(
                            if (state.eloChange7d > 0) "📈" else "📉",
                            "MOMENTUM",
                            if (state.eloChange7d > 0)
                                "You've gained ${state.eloChange7d} ELO recently. You're on the rise!"
                            else
                                "You've lost ${abs(state.eloChange7d)} ELO recently. Adjust your strategy.",
                            if (state.eloChange7d > 0) A_Green else A_Gold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightRow(icon: String, label: String, text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 14.sp)
        Column {
            Text(
                label, color = color, fontSize = 8.sp, letterSpacing = 2.sp,
                fontFamily = A_Mono, fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text, color = A_White.copy(0.72f), fontSize = 12.sp,
                fontFamily = A_Med, lineHeight = 18.sp
            )
        }
    }
}