package com.example.dsa_duel.screens.auth.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.R
import com.example.dsa_duel.data.PlayerStatsEntity
import com.example.dsa_duel.viewModels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlin.math.*

// ═══════════════════════════════════════════════════════════════
//  PALETTE & FONTS
// ═══════════════════════════════════════════════════════════════
private val P_BgDark = Color(0xFF020205)
private val P_CardBg = Color(0xFF0C0C18)
private val P_CardBg2 = Color(0xFF080612)
private val P_Purple = Color(0xFF8B5CF6)
private val P_PurpleDim = Color(0xFF6D28D9)
private val P_Cyan = Color(0xFF06B6D4)
private val P_CyanDim = Color(0xFF0891B2)
private val P_Gold = Color(0xFFFFB800)
private val P_Red = Color(0xFFEF4444)
private val P_Green = Color(0xFF22C55E)
private val P_White = Color(0xFFFFFFFF)
private val P_Gray = Color(0xFF6B7280)

private val P_MonoFont = FontFamily(Font(R.font.jetbrains_mono_medium))
private val P_BoldFont = FontFamily(Font(R.font.rajdhani_bold))
private val P_MedFont = FontFamily(Font(R.font.rajdhani_medium))

private fun pHex(cx: Float, cy: Float, r: Float): Path = Path().apply {
    for (i in 0..5) {
        val a = Math.toRadians(60.0 * i - 30.0)
        if (i == 0) moveTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
        else lineTo(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
    }
    close()
}

private val P_DSA =
    listOf("O(n)", "{}", "[]", "→", "∑", "λ", "log", "n²", "dp[", "BFS", "DFS", "++", "heap")

// ═══════════════════════════════════════════════════════════════
//  PROFILE SCREEN
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateAnalytics: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val stats by viewModel.playerStats.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    val inf = rememberInfiniteTransition(label = "profile")
    val codeRain by inf.animateFloat(
        0f,
        1000f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "cr"
    )
    val hexShimmer by inf.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(4000), RepeatMode.Reverse),
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
    val orbAngle by inf.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "oa"
    )

    Box(Modifier
        .fillMaxSize()
        .background(P_BgDark)) {

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
                        pHex(hcx, row * hH, hSz * 0.42f),
                        P_Purple.copy(0.018f + sh * 0.012f), style = Stroke(0.6f)
                    )
                }
            }
            val sl = scanLine * size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, P_Cyan.copy(0.05f), Color.Transparent),
                    startY = sl - 40f, endY = sl + 40f
                ),
                topLeft = Offset(0f, sl - 40f), size = Size(size.width, 80f)
            )
            val nc = drawContext.canvas.nativeCanvas
            val cols = 12;
            val cw = size.width / cols
            repeat(cols) { col ->
                repeat(7) { row ->
                    val ch = P_DSA[(col * 4 + row * 3) % P_DSA.size]
                    val by = ((row / 7f) + (codeRain / 1000f)) % 1f
                    val al = (1f - by) * 0.07f
                    if (al > 0.01f) {
                        nc.drawText(ch, col * cw, by * size.height, android.graphics.Paint().apply {
                            textSize = 8.dp.toPx()
                            color = when (col % 3) {
                                0 -> P_Purple.copy(al).toArgb()
                                1 -> P_Cyan.copy(al * 0.7f).toArgb()
                                else -> P_Green.copy(al * 0.5f).toArgb()
                            }
                            typeface = android.graphics.Typeface.MONOSPACE
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
                        listOf(P_Purple.copy(0.5f), Color.Transparent),
                        center = Offset(0f, 0f), radius = 650f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.04f + bgBreath * 0.02f)
                .background(
                    Brush.radialGradient(
                        listOf(P_CyanDim.copy(0.4f), Color.Transparent),
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
                                        Brush.verticalGradient(listOf(P_Purple, P_Cyan)),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                "PLAYER PROFILE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                fontFamily = P_MonoFont,
                                color = P_White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(P_Purple.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, P_Purple.copy(0.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, "Back",
                                    tint = P_White, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateAnalytics) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(P_Cyan.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, P_Cyan.copy(0.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.BarChart, "Analytics",
                                    tint = P_Cyan, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = onSignOut) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(P_Red.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.8.dp, P_Red.copy(0.35f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Logout, "Sign Out",
                                    tint = P_Red, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = P_White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(10.dp))

                stats?.let { s ->
                    ProfileAvatarSection(s, orbAngle)
                    Spacer(Modifier.height(28.dp))
                    ProfileSectionLabel("⚔  BATTLE STATISTICS")
                    Spacer(Modifier.height(12.dp))
                    ProfileStatsGrid(s)
                    Spacer(Modifier.height(24.dp))
                    ProfileSectionLabel("◈  ACCOUNT INFO")
                    Spacer(Modifier.height(12.dp))
                    ProfileInfoCard(s, viewModel)
                    Spacer(Modifier.height(24.dp))
                    ProfileSectionLabel("⚡  COMBAT RECORD")
                    Spacer(Modifier.height(12.dp))
                    CombatRecordBar(s)
                    Spacer(Modifier.height(36.dp))
                    ResetButton { showResetDialog = true }
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = P_CardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚠", fontSize = 20.sp)
                    Text(
                        "RESET ALL PROGRESS?", color = P_White,
                        fontFamily = P_MonoFont, fontSize = 14.sp, letterSpacing = 1.sp
                    )
                }
            },
            text = {
                Text(
                    "This will permanently delete your ELO, wins, and topic mastery. This cannot be undone.",
                    color = P_Gray, fontSize = 13.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = P_Red.copy(0.15f)),
                    border = BorderStroke(1.dp, P_Red.copy(0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "RESET", color = P_Red, fontFamily = P_MonoFont,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = P_Gray, fontFamily = P_MonoFont)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  AVATAR SECTION
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileAvatarSection(stats: PlayerStatsEntity, orbAngle: Float) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val inf = rememberInfiniteTransition(label = "avatar")
    val coronaPulse by inf.animateFloat(
        0.5f,
        1f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "cp"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + scaleIn(tween(500, easing = FastOutSlowInEasing), 0.85f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar with orbital rings
            Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f;
                    val cy = size.height / 2f
                    val outerR = size.width * 0.47f;
                    val innerR = size.width * 0.36f
                    rotate(orbAngle * 0.4f) {
                        drawPath(
                            pHex(cx, cy, outerR), P_Purple.copy(0.2f),
                            style = Stroke(
                                1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                            )
                        )
                    }
                    rotate(-orbAngle * 0.6f) {
                        drawPath(pHex(cx, cy, innerR), P_Cyan.copy(0.18f), style = Stroke(1.5f))
                    }
                    drawCircle(P_Purple.copy(coronaPulse * 0.2f), innerR * 0.72f, Offset(cx, cy))
                    drawCircle(P_Purple.copy(coronaPulse * 0.08f), outerR * 0.88f, Offset(cx, cy))
                    val r1 = Math.toRadians(orbAngle.toDouble())
                    val r2 = Math.toRadians((orbAngle + 150.0))
                    drawCircle(
                        P_Purple.copy(0.95f), 4.dp.toPx(),
                        Offset(cx + cos(r1).toFloat() * outerR, cy + sin(r1).toFloat() * outerR)
                    )
                    drawCircle(
                        P_Cyan.copy(0.55f), 2.5.dp.toPx(),
                        Offset(cx + cos(r2).toFloat() * innerR, cy + sin(r2).toFloat() * innerR)
                    )
                }
                Box(
                    Modifier
                        .size(78.dp)
                        .drawBehind {
                            drawCircle(
                                P_Purple.copy(coronaPulse * 0.28f),
                                size.maxDimension * 0.72f
                            )
                        }
                        .background(P_Purple.copy(0.1f), CircleShape)
                        .border(
                            BorderStroke(
                                2.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        P_Purple,
                                        P_Purple.copy(0.25f),
                                        P_Cyan.copy(0.5f),
                                        P_Purple
                                    )
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center) {
                    Text(stats.rankEmoji, fontSize = 38.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stats.displayName.uppercase(), color = P_White, fontSize = 26.sp,
                fontFamily = P_BoldFont, fontWeight = FontWeight.Black, letterSpacing = 2.sp
            )

            Spacer(Modifier.height(5.dp))

            Surface(
                color = P_Purple.copy(0.15f), shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.8.dp, P_Purple.copy(0.5f))
            ) {
                Text(
                    stats.rankName.uppercase(),
                    color = P_Purple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    fontFamily = P_MonoFont,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ELO display
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .matchParentSize()
                        .blur(14.dp)
                        .background(P_Purple.copy(0.25f), RoundedCornerShape(12.dp))
                )
                Surface(
                    color = P_CardBg2, shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp, Brush.horizontalGradient(
                            listOf(P_Purple.copy(0.5f), P_Cyan.copy(0.3f), P_Purple.copy(0.4f))
                        )
                    )
                ) {
                    Row(
                        Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚡", fontSize = 16.sp)
                        Text(
                            "${stats.eloRating}",
                            color = P_White,
                            fontSize = 28.sp,
                            fontFamily = P_BoldFont,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            modifier = Modifier.drawBehind {
                                drawCircle(
                                    P_Purple.copy(0.12f),
                                    size.maxDimension * 0.8f
                                )
                            })
                        Text(
                            "ELO", color = P_Purple, fontSize = 14.sp,
                            fontFamily = P_MonoFont, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SECTION LABEL
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileSectionLabel(text: String) {
    Row(
        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.7.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, P_Gray.copy(0.3f)))
                )
        )
        Text(
            text, color = P_Gray, fontSize = 9.sp, letterSpacing = 2.sp,
            fontFamily = P_MonoFont, fontWeight = FontWeight.Bold
        )
        Box(
            Modifier
                .weight(1f)
                .height(0.7.dp)
                .background(
                    Brush.horizontalGradient(listOf(P_Gray.copy(0.3f), Color.Transparent))
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  STATS GRID
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileStatsGrid(stats: PlayerStatsEntity) {
    val cards = listOf(
        Triple("WIN RATE", "${stats.winRate}%", P_Cyan),
        Triple("DUELS", "${stats.totalDuels}", P_Purple),
        Triple("BEST STREAK", "${stats.longestStreak}D 🔥", P_Gold),
        Triple("PEAK ELO", "${stats.peakElo}", P_White),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value, color) ->
                    PStatCard(label, value, color, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(160); visible = true }
    val inf = rememberInfiniteTransition(label = "psc$label")
    val glow by inf.animateFloat(
        0.35f,
        0.8f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "g"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + scaleIn(tween(380, easing = FastOutSlowInEasing), 0.9f)
    ) {
        Box(modifier) {
            Box(
                Modifier
                    .matchParentSize()
                    .blur(10.dp)
                    .background(color.copy(0.15f), RoundedCornerShape(16.dp))
            )
            Surface(
                color = P_CardBg2, 
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(110.dp),
                border = BorderStroke(
                    0.8.dp, Brush.linearGradient(
                        listOf(
                            color.copy(0.45f * glow),
                            color.copy(0.15f),
                            color.copy(0.3f * glow)
                        ),
                        start = Offset(0f, 0f), end = Offset.Infinite
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
                        Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            label, color = P_Gray, fontSize = 9.sp, letterSpacing = 2.sp,
                            fontFamily = P_MonoFont, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black,
                            fontFamily = P_BoldFont
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  ACCOUNT INFO CARD
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileInfoCard(stats: PlayerStatsEntity, viewModel: ProfileViewModel) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .blur(12.dp)
                    .background(P_Purple.copy(0.12f), RoundedCornerShape(16.dp))
            )
            Surface(
                color = P_CardBg2, shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    0.8.dp, Brush.linearGradient(
                        listOf(P_Purple.copy(0.35f), P_Cyan.copy(0.2f)),
                        start = Offset(0f, 0f), end = Offset.Infinite
                    )
                )
            ) {
                Box {
                    Canvas(Modifier.matchParentSize()) {
                        drawRect(
                            Brush.verticalGradient(
                                listOf(
                                    P_Purple.copy(0.05f),
                                    Color.Transparent
                                ), 0f, size.height * 0.4f
                            )
                        )
                    }
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PInfoRow(Icons.Default.Email, "EMAIL", stats.email)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(0.6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            P_Purple.copy(0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        PInfoRow(
                            Icons.Default.CalendarToday,
                            "MEMBER SINCE",
                            viewModel.formatDate(stats.createdAt)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(P_Purple.copy(0.1f), RoundedCornerShape(10.dp))
                .border(BorderStroke(0.6.dp, P_Purple.copy(0.3f)), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = P_Purple, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(
                label,
                color = P_Gray,
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                fontFamily = P_MonoFont
            )
            Text(
                value.ifBlank { "—" }, color = P_White, fontSize = 13.sp,
                fontFamily = P_MedFont, fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  COMBAT RECORD BAR
// ═══════════════════════════════════════════════════════════════
@Composable
private fun CombatRecordBar(stats: PlayerStatsEntity) {
    val wins = stats.totalWins
    val losses = stats.totalDuels - stats.totalWins
    val total = stats.totalDuels.coerceAtLeast(1)
    val winFraction = wins.toFloat() / total

    Box {
        Box(
            Modifier
                .matchParentSize()
                .blur(10.dp)
                .background(P_Purple.copy(0.1f), RoundedCornerShape(14.dp))
        )
        Surface(
            color = P_CardBg2, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.8.dp, P_Purple.copy(0.25f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${wins}W", color = P_Green, fontSize = 14.sp,
                        fontFamily = P_BoldFont, fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "${(winFraction * 100).toInt()}% WIN RATIO",
                        color = P_Gray,
                        fontSize = 9.sp,
                        fontFamily = P_MonoFont,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${losses}L", color = P_Red, fontSize = 14.sp,
                        fontFamily = P_BoldFont, fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                // W/L bar
                Box(Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))) {
                    Box(Modifier
                        .fillMaxSize()
                        .background(P_Red.copy(0.3f)))
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(winFraction)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        P_Green,
                                        P_Green.copy(0.7f)
                                    )
                                )
                            )
                    )
                    // divider tick
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(1.5.dp)
                            .fillMaxWidth(winFraction)
                            .align(Alignment.TopEnd)
                            .background(P_White.copy(0.8f))
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  RESET BUTTON
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ResetButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .blur(10.dp)
                .background(P_Red.copy(0.1f), RoundedCornerShape(12.dp))
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, P_Red.copy(0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = P_Red)
        ) {
            Icon(
                Icons.Default.DeleteForever, null,
                tint = P_Red, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "RESET ALL PROGRESS", color = P_Red, fontFamily = P_MonoFont,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, fontSize = 11.sp
            )
        }
    }
}