package com.example.dsa_duel.data

//this is the class that tells ui that our api is loading, succesful or throws error
data class AuthState(
    val isLoading: Boolean = false,
    val user: LoginResponse? = null,
    val error: String? = null,
    val forgotPasswordSuccess: Boolean = false,
    val resetPasswordSuccess: Boolean = false,
    val message: String? = null
)
