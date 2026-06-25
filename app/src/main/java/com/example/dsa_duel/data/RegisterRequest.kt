package com.example.dsa_duel.data

data class RegisterRequest (
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)