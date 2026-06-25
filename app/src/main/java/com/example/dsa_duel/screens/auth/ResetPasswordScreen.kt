package com.example.dsa_duel.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.viewModels.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun ResetPasswordScreen(
    email: String,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearState() }
    }

    AuthBackground(
        isLogin = true,
        onBack = onNavigateToLogin // 🔥 Added back button support
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
                text = "RESET PASSWORD",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter the code sent to $email",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Reset Code (OTP)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8F67FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = Color(0xFF8F67FF),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        if (otp.isNotBlank() && newPassword.isNotBlank()) {
                            viewModel.resetPassword(email, otp, newPassword)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F67FF))
                ) {
                    Text("UPDATE PASSWORD", fontWeight = FontWeight.Bold)
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

            if (authState.resetPasswordSuccess) {
                Text(
                    text = authState.message ?: "Password updated successfully!",
                    color = Color(0xFF10B981),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Bold
                )

                LaunchedEffect(Unit) {
                    delay(2000)
                    onNavigateToLogin()
                }
            }

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("BACK TO LOGIN", color = Color(0xFF00C2FF))
            }
        }
    }
}
