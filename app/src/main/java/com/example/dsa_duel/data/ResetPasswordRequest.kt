package com.example.dsa_duel.data

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
