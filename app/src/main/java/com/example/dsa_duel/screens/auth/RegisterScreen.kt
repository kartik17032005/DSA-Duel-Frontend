package com.example.dsa_duel.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dsa_duel.components.AuthCard
import com.example.dsa_duel.components.LoginRegisterToggle
import com.example.dsa_duel.data.LoginResponse
import com.example.dsa_duel.viewModels.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (LoginResponse) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.user) {
        authState.user?.let {
            onRegisterSuccess(it)
        }
    }

    AuthBackground(
        isLogin = false,
        onBack = onNavigateToLogin // 🔥 Added back button support
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection(isLogin = false)
            Spacer(modifier = Modifier.height(24.dp))
            LoginRegisterToggle(isLogin = false, onToggle = { if (it) onNavigateToLogin() })
            AuthCard(
                isLogin = false,
                onToggle = { if (it) onNavigateToLogin() },
                viewModel = viewModel
            )
        }
    }
}
