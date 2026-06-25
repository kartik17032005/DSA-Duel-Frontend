package com.example.dsa_duel.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.viewModels.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }

    // Navigation trigger on success
    LaunchedEffect(authState.forgotPasswordSuccess) {
        if (authState.forgotPasswordSuccess) {
            onNavigateToResetPassword(email)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearState() }
    }

    AuthBackground(
        isLogin = true,
        onBack = onNavigateBack // 🔥 Added back button support
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeaderSection(isLogin = true)
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "FORGOT PASSWORD",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your email to receive a reset link",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8F67FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF8F67FF),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (authState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF8F67FF))
            } else {
                Button(
                    onClick = { 
                        if (email.isNotBlank()) {
                            viewModel.forgotPassword(email) 
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F67FF))
                ) {
                    Text("SEND RESET LINK", fontWeight = FontWeight.Bold)
                }
            }

            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("BACK TO LOGIN", color = Color(0xFF00C2FF))
            }
        }
    }
}
