package com.example.dsa_duel.network.auth

import com.example.dsa_duel.data.ForgotPasswordRequest
import com.example.dsa_duel.data.LoginRequest
import com.example.dsa_duel.data.LoginResponse
import com.example.dsa_duel.data.RegisterRequest
import com.example.dsa_duel.data.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthAPI {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<Map<String, String>>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<Map<String, String>>
}
