package com.example.dsa_duel.screens.auth.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.R
import com.example.dsa_duel.data.TopicMasteryEntity
import com.example.dsa_duel.viewModels.HomeUiState
import com.example.dsa_duel.viewModels.HomeViewModel
import com.example.dsa_duel.viewModels.StreakDay
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Palette / Fonts
// ─────────────────────────────────────────────────────────────────────────────
private val DarkBg = Color(0xFF020205)
private val CardBg = Color(0xFF0B0D17)
private val CardBg2 = Color(0xFF101327)
private val CardBorder = Color(0xFF242842)

private val Purple = Color(0xFF8F67FF)
private val PurpleDim = Color(0xFF6D28D9)
private val Cyan = Color(0xFF00C2FF)
private val Gold = Color(0xFFFFB800)
private val Green = Color(0xFF22C55E)
private val White = Color(0xFFFFFFFF)
private val GrayMuted = Color(0xFF64748B)

private val MonoFont = FontFamily(Font(R.font.jetbrains_mono_medium))
private val BoldFont = FontFamily(Font(R.font.rajdhani_bold))
private val MedFont = FontFamily(Font(R.font.rajdhani_medium))

// ─────────────────────────────────────────────────────────────────────────────
// Home Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateDuel: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateRank: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNotAuthenticated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAllTopics by remember { mutableStateOf(false) }

    val inf = rememberInfiniteTransition(label = "home")
    val scanOffset by inf.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )
    val orbAngle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb"
    )

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            HomeBottomNavBar(
                selected = selectedTab,
                onHome = { selectedTab = 0 },
                onDuel = onNavigateDuel,
                onRank = onNavigateRank,
                onProfile = onNavigateProfile
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .homeBackground(scanOffset)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 10.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HomeHeader(
                        greeting = uiState.greeting,
                        name = uiState.displayName.uppercase(),
                        rankEmoji = uiState.rankEmoji,
                        onSignOut = onSignOut,
                        orbAngle = orbAngle
                    )
                }

                item { EloSummaryRow(uiState) }

                item { DuelActionCard(onClick = onNavigateDuel) }

                item { StreakCard(streak = uiState.currentStreak, days = uiState.streakDays) }

                item {
                    HomeSectionHeader(
                        title = "TOPIC MASTERY",
                        action = if (showAllTopics) "SHOW LESS ↑" else "VIEW ALL →",
                        onAction = { showAllTopics = !showAllTopics }
                    )
                }

                item { TopicGrid(topics = uiState.topicMastery, isExpanded = showAllTopics) }

                uiState.weakestTopic?.let { topic ->
                    item { AdaptiveHintCard(topic = topic, mastery = uiState.weakestTopicMastery) }
                }

                item { StatsRow(uiState) }
            }
        }
    }
}

private fun Modifier.homeBackground(scanOffset: Float): Modifier = drawWithCache {
    val base = Brush.verticalGradient(
        colors = listOf(DarkBg, Color(0xFF070A16), DarkBg)
    )

    val topGlow = Brush.radialGradient(
        colors = listOf(Purple.copy(alpha = 0.15f), Color.Transparent),
        center = Offset(size.width * 0.2f, size.height * 0.06f),
        radius = size.minDimension * 0.7f
    )

    val bottomGlow = Brush.radialGradient(
        colors = listOf(Cyan.copy(alpha = 0.09f), Color.Transparent),
        center = Offset(size.width * 0.95f, size.height * 0.9f),
        radius = size.minDimension * 0.7f
    )

    onDrawBehind {
        drawRect(base)
        drawRect(topGlow)
        drawRect(bottomGlow)

        val startX = size.width * scanOffset
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, White.copy(alpha = 0.028f), Color.Transparent),
                startX = startX,
                endX = startX + size.width * 0.22f
            ),
            topLeft = Offset.Zero,
            size = size
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoadingScreen() {
    val inf = rememberInfiniteTransition(label = "loading")
    val pulse by inf.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .drawBehind {
                    drawCircle(Purple.copy(0.18f), radius = size.minDimension * 0.6f * pulse)
                },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = Purple,
                trackColor = Purple.copy(0.14f),
                strokeWidth = 2.dp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    greeting: String,
    name: String,
    rankEmoji: String,
    onSignOut: () -> Unit,
    orbAngle: Float
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(70); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -16 }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier
                        .size(6.dp)
                        .background(Green, CircleShape))
                    Text(
                        "ONLINE  ·  DUEL_READY  ·  RANKED",
                        color = Cyan.copy(0.65f),
                        fontSize = 9.sp,
                        fontFamily = MonoFont,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(greeting, color = GrayMuted, fontSize = 13.sp, fontFamily = MedFont)
                Text(
                    name,
                    color = White,
                    fontSize = 27.sp,
                    fontFamily = BoldFont,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clickable(onClick = onSignOut),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .drawBehind {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.minDimension * 0.45f
                            drawCircle(Purple.copy(0.2f), radius = r, center = Offset(cx, cy))

                            val rad = Math.toRadians(orbAngle.toDouble())
                            val x = cx + cos(rad).toFloat() * r
                            val y = cy + sin(rad).toFloat() * r
                            drawCircle(Purple, radius = 3.2.dp.toPx(), center = Offset(x, y))
                        }
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Purple.copy(0.18f), CircleShape)
                        .border(1.dp, Purple.copy(0.48f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(rankEmoji, fontSize = 22.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ELO Summary
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EloSummaryRow(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1.6f),
            color = CardBg2,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(listOf(Purple.copy(0.4f), CardBorder, Cyan.copy(0.35f)))
            )
        ) {
            Column(Modifier.padding(14.dp)) {
                Surface(
                    color = Purple.copy(0.16f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.7.dp, Purple.copy(0.45f))
                ) {
                    Text(
                        "${uiState.rankEmoji}  ${uiState.rankName.uppercase()}",
                        color = Purple,
                        fontSize = 9.sp,
                        fontFamily = MonoFont,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "${uiState.eloRating}",
                    color = White,
                    fontSize = 32.sp,
                    fontFamily = BoldFont,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "ELO RATING",
                    color = GrayMuted,
                    fontSize = 8.sp,
                    fontFamily = MonoFont,
                    letterSpacing = 1.7.sp
                )

                Spacer(Modifier.height(10.dp))

                val progress = uiState.rankProgress.coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(CardBorder, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(listOf(Purple, Cyan)),
                                RoundedCornerShape(999.dp)
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NEXT RANK", color = GrayMuted, fontSize = 8.sp, fontFamily = MonoFont)
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = Purple,
                        fontSize = 8.sp,
                        fontFamily = MonoFont
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeMiniStatCard("WIN RATE", "${uiState.winRate}%", Green)
            HomeMiniStatCard("TOTAL WINS", "${uiState.totalWins}", Purple)
        }
    }
}

@Composable
private fun HomeMiniStatCard(label: String, value: String, color: Color) {
    Surface(
        color = CardBg2,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.height(66.dp).fillMaxWidth(),
        border = BorderStroke(0.8.dp, color.copy(0.33f))
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                color = color,
                fontSize = 21.sp,
                fontFamily = BoldFont,
                fontWeight = FontWeight.Black
            )
            Text(
                label,
                color = GrayMuted,
                fontSize = 8.sp,
                fontFamily = MonoFont,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Duel CTA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DuelActionCard(onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(140); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 18 }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(
                1.2.dp,
                Brush.linearGradient(listOf(Purple, PurpleDim, Cyan.copy(0.7f)))
            )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Purple.copy(0.30f), PurpleDim.copy(0.2f), Cyan.copy(0.1f))
                        )
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚔️", fontSize = 22.sp)
                        Text(
                            "FIND A DUEL",
                            color = White,
                            fontSize = 24.sp,
                            fontFamily = BoldFont,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "MATCHMAKING  ·  ELO RANKED  ·  LIVE",
                        color = White.copy(0.6f),
                        fontSize = 9.sp,
                        fontFamily = MonoFont,
                        letterSpacing = 1.7.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Streak
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StreakCard(streak: Int, days: List<StreakDay>) {
    val hasStreak = streak > 0
    val accent = if (hasStreak) Gold else GrayMuted

    Surface(
        color = CardBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            if (hasStreak) 1.dp else 0.8.dp,
            accent.copy(if (hasStreak) 0.55f else 0.35f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (hasStreak) "🔥" else "❄️", fontSize = if (hasStreak) 34.sp else 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (hasStreak) "$streak DAY STREAK" else "NO STREAK YET",
                        color = accent,
                        fontSize = 20.sp,
                        fontFamily = BoldFont
                    )
                    Text(
                        if (hasStreak) "KEEP THE FIRE ALIVE · DON'T BREAK THE CHAIN"
                        else "PLAY A DUEL TO IGNITE YOUR STREAK",
                        color = GrayMuted,
                        fontSize = 9.sp,
                        fontFamily = MonoFont,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEach { day ->
                    val tileColor = when {
                        day.isToday && day.isCompleted -> Gold
                        day.isCompleted -> Gold.copy(0.8f)
                        day.isToday -> CardBorder
                        else -> CardBorder.copy(0.65f)
                    }

                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tileColor)
                                .then(
                                    if (day.isToday) Modifier.border(
                                        1.dp,
                                        Gold.copy(0.65f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isCompleted) {
                                Text(
                                    "✓",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            day.label,
                            color = if (day.isCompleted) Gold else if (day.isToday) White else GrayMuted,
                            fontSize = 8.sp,
                            fontFamily = MonoFont,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Topic Mastery
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TopicGrid(topics: List<TopicMasteryEntity>, isExpanded: Boolean) {
    if (topics.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading topics…", color = GrayMuted, fontSize = 13.sp, fontFamily = MonoFont)
        }
        return
    }

    val displayed = if (isExpanded) topics else topics.take(6)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        displayed.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { topic ->
                    TopicCard(topic = topic, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun TopicCard(topic: TopicMasteryEntity, modifier: Modifier = Modifier) {
    val accent = topicColor(topic.topic)
    val icon = topicIcon(topic.topic)

    Surface(
        modifier = modifier.height(146.dp),
        color = if (topic.isUnlocked) CardBg2 else Color(0xFF0A0C16),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (topic.isUnlocked) 1.dp else 0.6.dp,
            if (topic.isUnlocked) accent.copy(0.42f) else CardBorder.copy(0.5f)
        )
    ) {
        Column(Modifier
            .fillMaxSize()
            .padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 24.sp)

                if (!topic.isUnlocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = GrayMuted.copy(0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                } else if (topic.questionsAttempted > 0) {
                    Surface(
                        color = accent.copy(0.14f),
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.5.dp, accent.copy(0.5f))
                    ) {
                        Text(
                            topic.masteryLevel,
                            color = accent,
                            fontSize = 7.sp,
                            fontFamily = MonoFont,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                topic.topic,
                color = if (topic.isUnlocked) White else GrayMuted.copy(0.6f),
                fontSize = 15.sp,
                fontFamily = BoldFont,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.weight(1f))

            if (topic.isUnlocked) {
                val mastery = topic.masteryScore.coerceIn(0f, 1f)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(CardBorder, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(mastery)
                            .background(
                                Brush.horizontalGradient(listOf(accent.copy(0.7f), accent)),
                                RoundedCornerShape(999.dp)
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${topic.masteryPercent}%",
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MonoFont
                    )
                    Text(
                        "${topic.questionsCorrect}/${topic.questionsAttempted.coerceAtLeast(1)}",
                        color = GrayMuted,
                        fontSize = 10.sp,
                        fontFamily = MonoFont
                    )
                }
            } else {
                Text(
                    unlockHint(topic.topic),
                    color = GrayMuted.copy(0.5f),
                    fontSize = 9.sp,
                    fontFamily = MonoFont,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AdaptiveHintCard(topic: String, mastery: Int) {
    Surface(
        color = CardBg2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.8.dp, Purple.copy(0.4f))
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Purple.copy(0.18f), CircleShape)
                    .border(0.8.dp, Purple.copy(0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💡", fontSize = 16.sp)
            }

            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "FOCUS AREA",
                        color = Purple.copy(0.8f),
                        fontSize = 8.sp,
                        fontFamily = MonoFont,
                        letterSpacing = 1.7.sp
                    )

                    Surface(color = Purple.copy(0.14f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "$mastery%",
                            color = Purple,
                            fontSize = 8.sp,
                            fontFamily = MonoFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    "$topic is your weakest topic. Battle more here to rank up faster.",
                    color = White.copy(0.78f),
                    fontSize = 12.sp,
                    fontFamily = MedFont
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(uiState: HomeUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeStatBox("DUELS", "${uiState.totalDuels}", Modifier.weight(1f), Cyan)
        HomeStatBox("BEST STK", "${uiState.longestStreak}🔥", Modifier.weight(1f), Gold)
        HomeStatBox(
            "TOPICS",
            "${uiState.unlockedTopicCount}/${uiState.totalTopicCount}",
            Modifier.weight(1f),
            Purple
        )
    }
}

@Composable
private fun HomeStatBox(label: String, value: String, modifier: Modifier, accent: Color = Purple) {
    Surface(
        modifier = modifier.height(76.dp),
        color = CardBg2,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, accent.copy(0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                color = White,
                fontSize = 19.sp,
                fontFamily = BoldFont,
                fontWeight = FontWeight.Black
            )
            Text(
                label,
                color = GrayMuted,
                fontSize = 8.sp,
                fontFamily = MonoFont,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeSectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 16.dp)
                    .background(
                        Brush.verticalGradient(listOf(Purple, Cyan)),
                        RoundedCornerShape(2.dp)
                    )
            )
            Text(
                title,
                color = White,
                fontSize = 15.sp,
                fontFamily = BoldFont,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Surface(
            color = Cyan.copy(0.10f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(0.7.dp, Cyan.copy(0.4f)),
            modifier = Modifier.clickable(onClick = onAction)
        ) {
            Text(
                action,
                color = Cyan,
                fontSize = 10.sp,
                fontFamily = BoldFont,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Nav
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeBottomNavBar(
    selected: Int,
    onHome: () -> Unit,
    onDuel: () -> Unit,
    onRank: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .height(66.dp),
        shape = RoundedCornerShape(22.dp),
        color = CardBg2,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(Purple.copy(0.3f), Cyan.copy(0.2f), Purple.copy(0.2f)))
        )
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeNavItem("HOME", Icons.Default.Home, selected == 0, onHome)
            HomeNavItem("DUEL", Icons.Default.SportsKabaddi, selected == 1, onDuel, Gold)
            HomeNavItem("RANK", Icons.Default.EmojiEvents, selected == 2, onRank)
            HomeNavItem("PROFILE", Icons.Default.Person, selected == 3, onProfile)
        }
    }
}

@Composable
fun HomeNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = Purple
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            Modifier
                .size(width = 42.dp, height = 28.dp)
                .drawBehind {
                    if (isSelected) {
                        drawRoundRect(
                            color = accentColor.copy(0.24f),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else GrayMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (isSelected) accentColor else GrayMuted,
            fontSize = 7.5.sp,
            fontFamily = MonoFont,
            letterSpacing = 0.5.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Topic Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun topicColor(topic: String): Color = when (topic) {
    "Arrays" -> Color(0xFF8F67FF)
    "LinkedList", "Strings" -> Color(0xFF00C2FF)
    "Stacks" -> Color(0xFFFF8C00)
    "Trees" -> Color(0xFFFFB800)
    "Sorting" -> Color(0xFFFF6B6B)
    "BinarySearch" -> Color(0xFF4CAF50)
    "Graphs", "DP" -> Color(0xFFE91E63)
    else -> Color(0xFF8F67FF)
}

private fun topicIcon(topic: String): String = when (topic) {
    "Arrays" -> "🌳"
    "LinkedList" -> "🔗"
    "Strings" -> "📝"
    "Stacks" -> "📚"
    "Trees" -> "🌲"
    "Sorting" -> "🔀"
    "BinarySearch" -> "🔍"
    "Graphs" -> "🕸️"
    "DP" -> "🧠"
    else -> "⚔️"
}

private fun unlockHint(topic: String): String = when (topic) {
    "LinkedList", "Strings" -> "Unlock: Arrays ≥ 40%"
    "Stacks" -> "Unlock: LinkedList ≥ 40%"
    "Trees" -> "Unlock: Arrays + LinkedList ≥ 50%"
    "Graphs" -> "Unlock: Trees ≥ 50%"
    "Sorting" -> "Unlock: Arrays + Stacks ≥ 40%"
    "BinarySearch" -> "Unlock: Sorting ≥ 50%"
    "DP" -> "Unlock: Trees + Graphs ≥ 50%"
    else -> "Complete prerequisites"
}