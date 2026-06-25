package com.example.dsa_duel.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.R
import com.example.dsa_duel.viewModels.AuthViewModel

@Composable
fun AuthCard(
    isLogin: Boolean,
    onToggle: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit = {}, // 🔥 Added this
    viewModel: AuthViewModel
) {
    val warriorNameState = rememberSaveable { mutableStateOf("") }
    val emailState = rememberSaveable { mutableStateOf("") }
    val passwordState = rememberSaveable { mutableStateOf("") }
    val confirmPasswordState = rememberSaveable { mutableStateOf("") }

    val localErrorState = remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val authState = viewModel.authState.collectAsState().value

    LaunchedEffect(isLogin) {
        localErrorState.value = null
    }

    val neonCyan = Color(0xFF00C2FF)
    val neonBlue = Color(0xFF4891FF)
    val neonPurple = Color(0xFF8F67FF)
    val neonDeepPurple = Color(0xFF6200EE)
    val errorRed = Color(0xFFFF4D4D)

    val activeColor = if (isLogin) neonPurple else neonCyan
    val secondaryActiveColor = if (isLogin) neonDeepPurple else neonBlue
    val darkCard = Color(0xFF0A0A1C).copy(alpha = 0.9f)
    val mutedIndigo = Color(0xFF6C6C82)

    val displayError = localErrorState.value ?: authState.error?.let {
        if (it.contains("Validation failed", ignoreCase = true) || it.contains(
                "DTO",
                ignoreCase = true
            )
        ) {
            "Please ensure all fields are filled correctly."
        } else it
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        colors = CardDefaults.cardColors(containerColor = darkCard),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF202038)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isLogin) "Enter the Arena" else "New Challenger",
                    fontSize = 32.sp,
                    color = activeColor,
                    fontFamily = FontFamily(Font(R.font.rajdhani_bold))
                )
                Text(
                    text = if (isLogin) "Prove your logic. Dominate the arena." else "Your legend begins here.",
                    fontSize = 14.sp,
                    color = mutedIndigo,
                    fontFamily = FontFamily(Font(R.font.rajdhani_medium)),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isLogin) {
                InputTextField(
                    valueState = warriorNameState,
                    labelId = "Warrior name",
                    enabled = !authState.isLoading,
                    leadingIcon = Icons.Filled.Person
                )
            }

            InputTextField(
                valueState = emailState,
                labelId = "Email address",
                enabled = !authState.isLoading,
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Filled.Email
            )

            InputTextField(
                valueState = passwordState,
                labelId = "Password",
                enabled = !authState.isLoading,
                keyboardType = KeyboardType.Password,
                leadingIcon = Icons.Filled.Lock
            )

            if (!isLogin) {
                InputTextField(
                    valueState = confirmPasswordState,
                    labelId = "Confirm password",
                    enabled = !authState.isLoading,
                    keyboardType = KeyboardType.Password,
                    leadingIcon = Icons.Filled.Lock
                )
            } else {
                Text(
                    text = "Forgot password?",
                    color = neonPurple.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.rajdhani_medium)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { onForgotPasswordClick() }, // 🔥 Wired here
                    textAlign = TextAlign.End
                )
            }

            if (displayError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(errorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, errorRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ ${displayError.uppercase()}",
                        color = errorRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily(Font(R.font.jetbrains_mono_medium)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    localErrorState.value = null
                    val email = emailState.value.trim()
                    val password = passwordState.value.trim()

                    if (email.isEmpty() || password.isEmpty()) {
                        localErrorState.value = "Email and Password are required"
                        return@Button
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        localErrorState.value = "Invalid email address format"
                        return@Button
                    }

                    if (isLogin) {
                        viewModel.login(email, password)
                    } else {
                        val name = warriorNameState.value.trim()
                        val confirm = confirmPasswordState.value.trim()

                        if (name.isEmpty()) {
                            localErrorState.value = "Warrior name is required"
                        } else if (password != confirm) {
                            localErrorState.value = "Passwords do not match"
                        } else if (password.length < 6) {
                            localErrorState.value = "Password must be at least 6 characters"
                        } else {
                            viewModel.register(name, email, password, confirm)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = activeColor,
                        spotColor = activeColor
                    ),
                enabled = !authState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    activeColor,
                                    secondaryActiveColor
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLogin) {
                                Text(text = "⚔️", fontSize = 18.sp)
                            } else {
                                Icon(
                                    Icons.Outlined.Shield,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLogin) "START DUEL" else "ENTER ARENA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily(Font(R.font.rajdhani_bold))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 0.5.dp,
                    color = Color(0xFF202038)
                )
                Text(
                    text = " or enter via ",
                    color = mutedIndigo,
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.rajdhani_medium)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 0.5.dp,
                    color = Color(0xFF202038)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialLoginButton(
                    modifier = Modifier.weight(1f),
                    text = "Google",
                    icon = R.drawable.google
                )
                SocialLoginButton(
                    modifier = Modifier.weight(1f),
                    text = "GitHub",
                    icon = R.drawable.social
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    if (isLogin) {
                        withStyle(style = SpanStyle(color = mutedIndigo)) { append("New challenger? ") }
                        withStyle(
                            style = SpanStyle(
                                color = neonPurple,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("Join the arena →") }
                    } else {
                        withStyle(style = SpanStyle(color = mutedIndigo)) { append("Already a warrior? ") }
                        withStyle(
                            style = SpanStyle(
                                color = neonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("← Sign in") }
                    }
                },
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.rajdhani_medium)),
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onToggle(!isLogin) }
            )
        }
    }
}

@Composable
fun SocialLoginButton(modifier: Modifier = Modifier, text: String, icon: Int) {
    Card(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF202038))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(id = icon),
                text,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.rajdhani_bold))
            )
        }
    }
}
