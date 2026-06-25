package com.example.dsa_duel.data

data class LoginResponse (
    val token: String? = null,
    val userId: Long? = null,
    val fullName: String? = null,
    val email: String? = null,
    val message: String? = null
)
