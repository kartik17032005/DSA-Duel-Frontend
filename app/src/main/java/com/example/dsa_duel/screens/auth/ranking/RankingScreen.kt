package com.example.dsa_duel.screens.auth.ranking

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.dsa_duel.viewModels.LeaderboardEntry
import com.example.dsa_duel.viewModels.RankingViewModel
import kotlinx.coroutines.delay
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════
//  PALETTE & FONTS
// ═══════════════════════════════════════════════════════════════
private val R_BgDark = Color(0xFF020205)
private val R_CardBg = Color(0xFF0C0C18)
private val R_CardBg2 = Color(0xFF080612)
private val R_Purple = Color(0xFF8B5CF6)
private val R_Cyan = Color(0xFF06B6D4)
private val R_CyanDim = Color(0xFF0891B2)
private val R_Gold = Color(0xFFFFB800)
private val R_Silver = Color(0xFFB0C4D8)
private val R_Bronze = Color(0xFFCD7F32)
private val R_Red = Color(0xFFEF4444)
private val R_Green = Color(0xFF22C55E)
private val R_White = Color(0xFFFFFFFF)
private val R_Gray = Color(0xFF6B7280)

private val R_MonoFont = FontFamily(Font(R.font.jetbrains_mono_medium))
private val R_BoldFont = FontFamily(Font(R.font.rajdhani_bold))

private fun rHex(cx: Float, cy: Float, r: Float): Path = Path().apply {
    for (i in 0..5) {
        val a = Math.toRadians(60.0 * i - 30.0)
        if (i == 0) moveTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
        else lineTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
    }
    close()
}

private val R_DSA =
    listOf("O(n)", "{}", "[]", "→", "∑", "λ", "log", "n²", "BFS", "DFS", "dp[", "heap")

private fun rankColor(rank: Int) = when (rank) {
    1 -> Color(0xFFFFB800)   // Gold
    2 -> Color(0xFFB0C4D8)   // Silver
    3 -> Color(0xFFCD7F32)   // Bronze
    else -> Color(0xFF6B7280)   // Gray
}

private fun rankAccent(rank: Int) = when (rank) {
    1 -> Color(0xFFFFB800)
    2 -> Color(0xFFB0C4D8)
    3 -> Color(0xFFCD7F32)
    else -> Color(0xFF8B5CF6)   // Purple for player
}

// ═══════════════════════════════════════════════════════════════
//  RANKING SCREEN
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onBack: () -> Unit,
    viewModel: RankingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val inf = rememberInfiniteTransition(label = "rank")
    val codeRain by inf.animateFloat(
        0f,
        1000f,
        infiniteRepeatable(tween(8500, easing = LinearEasing)),
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
        infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "bb"
    )
    val scanLine by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "sl"
    )
    val orbAngle by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "oa"
    )

    Box(Modifier
        .fillMaxSize()
        .background(R_BgDark)) {

        // ── BG layer ─────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            val hSz = 30.dp.toPx();
            val hH = hSz * sqrt(3f) / 2f
            repeat((size.height / hH).toInt() + 2) { row ->
                repeat((size.width / (hSz * 1.5f)).toInt() + 2) { col ->
                    val hcx = col * hSz * 1.5f + if (row % 2 == 1) hSz * 0.75f else 0f
                    val sh =
                        (sin(hexShimmer * PI * 2 + col * 0.4 + row * 0.3) * 0.5 + 0.5).toFloat()
                    drawPath(
                        rHex(hcx, row * hH, hSz * 0.42f),
                        R_Gold.copy(0.012f + sh * 0.010f), style = Stroke(0.6f)
                    )
                }
            }
            val sl = scanLine * size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, R_Gold.copy(0.05f), Color.Transparent),
                    startY = sl - 40f, endY = sl + 40f
                ),
                topLeft = Offset(0f, sl - 40f), size = Size(size.width, 80f)
            )
            val nc = drawContext.canvas.nativeCanvas
            val cols = 12;
            val cw = size.width / cols
            repeat(cols) { col ->
                repeat(7) { row ->
                    val ch = R_DSA[(col * 4 + row * 3) % R_DSA.size]
                    val by = ((row / 7f) + (codeRain / 1000f)) % 1f
                    val al = (1f - by) * 0.07f
                    if (al > 0.01f) {
                        nc.drawText(ch, col * cw, by * size.height, android.graphics.Paint().apply {
                            textSize = 8.dp.toPx()
                            color = when (col % 3) {
                                0 -> R_Gold.copy(al * 0.7f).toArgb()
                                1 -> R_Purple.copy(al).toArgb()
                                else -> R_Cyan.copy(al * 0.5f).toArgb()
                            }
                            typeface = android.graphics.Typeface.MONOSPACE
                        })
                    }
                }
            }
        }
        // Atmospheric gold/purple wash
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.05f + bgBreath * 0.03f)
                .background(
                    Brush.radialGradient(
                        listOf(R_Gold.copy(0.4f), Color.Transparent),
                        center = Offset.Infinite, radius = 700f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.04f + bgBreath * 0.02f)
                .background(
                    Brush.radialGradient(
                        listOf(R_Purple.copy(0.4f), Color.Transparent),
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
                                        Brush.verticalGradient(listOf(R_Gold, R_Purple)),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                "GLOBAL RANKINGS", fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp,
                                fontFamily = R_MonoFont, color = R_White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(R_Purple.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, R_Purple.copy(0.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, "Back",
                                    tint = R_White, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = R_White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    // Your rank hero card
                    YourRankCard(elo = uiState.playerElo, orbAngle = orbAngle)
                    Spacer(Modifier.height(20.dp))
                }

                // Top 3 Podium
                if (uiState.leaderboard.size >= 3) {
                    item {
                        PodiumSection(
                            first = uiState.leaderboard[0],
                            second = uiState.leaderboard[1],
                            third = uiState.leaderboard[2]
                        )
                        Spacer(Modifier.height(16.dp))
                        // Section divider
                        Row(
                            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(0.7.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                R_Gray.copy(0.3f)
                                            )
                                        )
                                    )
                            )
                            Text(
                                "ALL COMBATANTS", color = R_Gray, fontSize = 9.sp,
                                letterSpacing = 2.sp, fontFamily = R_MonoFont
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(0.7.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                R_Gray.copy(0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                itemsIndexed(uiState.leaderboard) { index, entry ->
                    LeaderboardRow(entry = entry, index = index)
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  YOUR RANK HERO CARD
// ═══════════════════════════════════════════════════════════════
@Composable
private fun YourRankCard(elo: Int, orbAngle: Float) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val inf = rememberInfiniteTransition(label = "yrk")
    val glow by inf.animateFloat(
        0.45f,
        0.9f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "g"
    )
    val shimmer by inf.animateFloat(
        -0.3f,
        1.3f,
        infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "sh"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450)) + slideInVertically(
            tween(
                450,
                easing = FastOutSlowInEasing
            )
        ) { -30 }) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .blur(18.dp)
                    .background(R_Purple.copy(glow * 0.35f), RoundedCornerShape(20.dp))
            )
            Surface(
                color = R_CardBg2, shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(R_Purple.copy(0.6f), R_Cyan.copy(0.3f), R_Purple.copy(0.4f)),
                        start = Offset(0f, 0f), end = Offset.Infinite
                    )
                )
            ) {
                Box {
                    // Shimmer sweep
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        R_White.copy(0.04f),
                                        Color.Transparent
                                    ),
                                    startX = shimmer * 600f, endX = (shimmer + 0.3f) * 600f
                                )
                            )
                    )
                    Canvas(Modifier.matchParentSize()) {
                        drawRect(
                            Brush.verticalGradient(
                                listOf(
                                    R_Purple.copy(0.08f),
                                    Color.Transparent
                                ), 0f, size.height * 0.5f
                            )
                        )
                    }
                    Row(
                        Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Orbital icon
                        Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                            Canvas(Modifier.fillMaxSize()) {
                                val cx = size.width / 2f;
                                val cy = size.height / 2f;
                                val r = size.width * 0.44f
                                rotate(orbAngle * 0.5f) {
                                    drawPath(
                                        rHex(cx, cy, r), R_Purple.copy(0.22f),
                                        style = Stroke(
                                            1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(
                                                    8f,
                                                    7f
                                                )
                                            )
                                        )
                                    )
                                }
                                val rad = Math.toRadians(orbAngle.toDouble())
                                drawCircle(
                                    R_Purple.copy(0.9f), 3.5.dp.toPx(),
                                    Offset(cx + cos(rad).toFloat() * r, cy + sin(rad).toFloat() * r)
                                )
                                drawCircle(R_Purple.copy(glow * 0.2f), r * 0.6f, Offset(cx, cy))
                            }
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .background(R_Purple.copy(0.12f), CircleShape)
                                    .border(
                                        BorderStroke(
                                            1.5.dp,
                                            Brush.sweepGradient(
                                                listOf(
                                                    R_Purple,
                                                    R_Purple.copy(0.2f),
                                                    R_Cyan.copy(0.4f),
                                                    R_Purple
                                                )
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp, null,
                                    tint = R_Purple, modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                "YOUR COMBAT RATING", color = R_Gray, fontSize = 8.sp,
                                letterSpacing = 2.sp, fontFamily = R_MonoFont
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "$elo",
                                    color = R_White,
                                    fontSize = 30.sp,
                                    fontFamily = R_BoldFont,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp,
                                    modifier = Modifier.drawBehind {
                                        drawCircle(
                                            R_Purple.copy(0.1f),
                                            size.maxDimension * 0.8f
                                        )
                                    })
                                Text(
                                    "ELO", color = R_Purple, fontSize = 14.sp,
                                    fontFamily = R_MonoFont, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        // Status badge
                        Surface(
                            color = R_Green.copy(0.1f), shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.6.dp, R_Green.copy(0.4f))
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(5.dp)
                                        .background(R_Green, CircleShape)
                                        .drawBehind {
                                            drawCircle(
                                                R_Green.copy(0.5f),
                                                size.maxDimension
                                            )
                                        })
                                Text(
                                    "LIVE", color = R_Green, fontSize = 8.sp,
                                    fontFamily = R_MonoFont, fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
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
//  PODIUM  (Top 3)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PodiumSection(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(
            tween(
                500,
                easing = FastOutSlowInEasing
            )
        ) { 40 }) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .blur(16.dp)
                    .background(R_Gold.copy(0.15f), RoundedCornerShape(22.dp))
            )
            Surface(
                color = R_CardBg2, shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(R_Gold.copy(0.4f), R_Silver.copy(0.2f), R_Bronze.copy(0.25f)),
                        start = Offset(0f, 0f), end = Offset.Infinite
                    )
                )
            ) {
                Box {
                    Canvas(Modifier.matchParentSize()) {
                        drawRect(
                            Brush.verticalGradient(
                                listOf(
                                    R_Gold.copy(0.06f),
                                    Color.Transparent
                                ), 0f, size.height * 0.4f
                            )
                        )
                    }
                    Column(Modifier.padding(16.dp)) {
                        // Header
                        Row(
                            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "HALL OF LEGENDS",
                                color = R_Gold,
                                fontSize = 10.sp,
                                letterSpacing = 3.sp,
                                fontFamily = R_MonoFont,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("🏆", fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        // Podium layout: 2nd | 1st | 3rd
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            PodiumEntry(second, 2, Modifier.weight(1f), heightDp = 90)
                            PodiumEntry(first, 1, Modifier.weight(1f), heightDp = 116)
                            PodiumEntry(third, 3, Modifier.weight(1f), heightDp = 78)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumEntry(
    entry: LeaderboardEntry,
    rank: Int,
    modifier: Modifier,
    heightDp: Int
) {
    val color = rankColor(rank)
    val medal = when (rank) {
        1 -> "🥇"; 2 -> "🥈"; else -> "🥉"
    }
    val inf = rememberInfiniteTransition(label = "pod$rank")
    val glow by inf.animateFloat(
        0.35f,
        0.8f,
        infiniteRepeatable(tween(900 + rank * 120), RepeatMode.Reverse),
        label = "g"
    )

    Column(
        modifier.height(heightDp.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(medal, fontSize = if (rank == 1) 28.sp else 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            entry.name.take(9), color = R_White, fontSize = if (rank == 1) 11.sp else 10.sp,
            fontFamily = R_BoldFont, fontWeight = FontWeight.ExtraBold,
            maxLines = 1, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(3.dp))
        Surface(
            color = color.copy(0.15f), shape = RoundedCornerShape(4.dp),
            border = BorderStroke(0.6.dp, color.copy(0.5f))
        ) {
            Text(
                "${entry.elo}", color = color, fontSize = 10.sp,
                fontFamily = R_MonoFont, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        // Podium block
        Box(
            Modifier
                .fillMaxWidth()
                .height((if (rank == 1) 36 else if (rank == 2) 24 else 16).dp)
                .drawBehind { drawRect(color.copy(glow * 0.3f)) }
                .background(
                    Brush.verticalGradient(listOf(color.copy(0.35f), color.copy(0.12f))),
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
                .border(
                    BorderStroke(0.6.dp, color.copy(0.5f)),
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                ),
            contentAlignment = Alignment.Center) {
            Text(
                "#$rank", color = color, fontSize = 10.sp,
                fontFamily = R_MonoFont, fontWeight = FontWeight.Black
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEADERBOARD ROW  (rank 4+)
// ═══════════════════════════════════════════════════════════════
@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60L + index * 50L); visible = true }

    val isPlayer = entry.isPlayer
    val isTop3 = entry.rank <= 3
    val accent = if (isPlayer) R_Purple else rankAccent(entry.rank)

    val inf = rememberInfiniteTransition(label = "lr${entry.rank}")
    val playerGlow by inf.animateFloat(
        0.4f, 0.85f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pg"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInHorizontally(
            tween(
                320,
                easing = FastOutSlowInEasing
            )
        ) { -30 }) {
        Box {
            if (isPlayer) {
                Box(
                    Modifier
                        .matchParentSize()
                        .blur(12.dp)
                        .background(R_Purple.copy(playerGlow * 0.25f), RoundedCornerShape(16.dp))
                )
            } else if (isTop3) {
                Box(
                    Modifier
                        .matchParentSize()
                        .blur(8.dp)
                        .background(accent.copy(0.12f), RoundedCornerShape(16.dp))
                )
            }

            Surface(
                color = when {
                    isPlayer -> R_Purple.copy(0.1f)
                    isTop3 -> accent.copy(0.06f)
                    else -> R_CardBg
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    if (isPlayer || isTop3) 1.dp else 0.6.dp,
                    when {
                        isPlayer -> Brush.linearGradient(
                            listOf(R_Purple.copy(0.7f), R_Purple.copy(0.3f), R_Cyan.copy(0.3f)),
                            start = Offset(0f, 0f), end = Offset.Infinite
                        )

                        isTop3 -> Brush.linearGradient(
                            listOf(accent.copy(0.5f), accent.copy(0.2f)),
                            start = Offset(0f, 0f), end = Offset.Infinite
                        )

                        else -> Brush.linearGradient(
                            listOf(
                                R_White.copy(0.06f),
                                R_White.copy(0.04f)
                            )
                        )
                    }
                )
            ) {
                Box {
                    if (isPlayer) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            R_Purple.copy(0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Canvas(Modifier.matchParentSize()) {
                            drawLine(
                                R_Purple.copy(0.7f),
                                Offset(0f, 0f),
                                Offset(size.width * 0.22f, 0f),
                                1.8f,
                                StrokeCap.Round
                            )
                        }
                    }
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Rank badge
                        Box(
                            Modifier
                                .size(36.dp)
                                .drawBehind {
                                    if (isTop3 || isPlayer) drawCircle(
                                        accent.copy(0.2f),
                                        size.maxDimension * 0.6f
                                    )
                                }
                                .background(accent.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(
                                    BorderStroke(
                                        if (isTop3 || isPlayer) 0.8.dp else 0.5.dp,
                                        accent.copy(if (isTop3 || isPlayer) 0.55f else 0.2f)
                                    ),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center) {
                            Text(
                                "#${entry.rank}", color = accent, fontSize = 11.sp,
                                fontWeight = FontWeight.Black, fontFamily = R_MonoFont
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Name
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isPlayer) "YOU" else entry.name,
                                color = if (isPlayer) R_Purple else R_White,
                                fontWeight = if (isPlayer) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = R_BoldFont
                            )
                            if (isPlayer) {
                                Text(
                                    "← YOUR POSITION", color = R_Purple.copy(0.6f),
                                    fontSize = 8.sp, fontFamily = R_MonoFont, letterSpacing = 1.sp
                                )
                            }
                        }

                        // ELO + medal/trophy
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "${entry.elo}",
                                color = if (isPlayer) R_Purple else R_White,
                                fontFamily = R_MonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            when (entry.rank) {
                                1 -> Text("🥇", fontSize = 14.sp)
                                2 -> Text("🥈", fontSize = 14.sp)
                                3 -> Text("🥉", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}