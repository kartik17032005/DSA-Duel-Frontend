package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.AuthState
import com.example.dsa_duel.data.LoginRequest
import com.example.dsa_duel.data.RegisterRequest
import com.example.dsa_duel.data.ResetPasswordRequest
import com.example.dsa_duel.repositories.AuthRepository
import com.example.dsa_duel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = repository.loginUser(
                LoginRequest(email, password)
            )) {
                is Resource.Success -> {
                    _authState.value = AuthState(user = result.data)
                }

                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = repository.registerUser(
                RegisterRequest(fullName, email, password, confirmPassword)
            )) {
                is Resource.Success -> {
                    _authState.value = AuthState(user = result.data)
                }

                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            when (val result = repository.forgotPassword(email)) {
                is Resource.Success -> {
                    _authState.value = AuthState(
                        forgotPasswordSuccess = true,
                        message = result.data ?: "Reset code sent to your email"
                    )
                }
                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPw: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            when (val result = repository.resetPassword(ResetPasswordRequest(email, otp, newPw))) {
                is Resource.Success -> {
                    _authState.value = AuthState(
                        resetPasswordSuccess = true,
                        message = result.data ?: "Password reset successfully"
                    )
                }
                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearState() {
        _authState.value = AuthState()
    }
}
