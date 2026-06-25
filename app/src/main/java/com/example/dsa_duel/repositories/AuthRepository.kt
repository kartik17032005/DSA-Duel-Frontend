package com.example.dsa_duel.repositories

import com.example.dsa_duel.data.ForgotPasswordRequest
import com.example.dsa_duel.data.LoginRequest
import com.example.dsa_duel.data.LoginResponse
import com.example.dsa_duel.data.RegisterRequest
import com.example.dsa_duel.data.ResetPasswordRequest
import com.example.dsa_duel.network.auth.RetrofitInstance
import com.example.dsa_duel.utils.Resource
import org.json.JSONObject

class AuthRepository {
    private val api = RetrofitInstance.api

    suspend fun registerUser(request: RegisterRequest): Resource<LoginResponse> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body())
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try { JSONObject(it).getString("message") } catch (e: Exception) { null }
                } ?: response.message()
                Resource.Error(if (errorMsg.isNullOrEmpty()) "Registration Failed" else errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun loginUser(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try { JSONObject(it).getString("message") } catch (e: Exception) { null }
                } ?: response.message()
                Resource.Error(if (errorMsg.isNullOrEmpty()) "Login Failed" else errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun forgotPassword(email: String): Resource<String> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["message"] ?: "Success")
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try { JSONObject(it).getString("message") } catch (e: Exception) { null }
                } ?: response.message()
                Resource.Error(if (errorMsg.isNullOrEmpty()) "Failed to send reset link" else errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Resource<String> {
        return try {
            val response = api.resetPassword(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["message"] ?: "Password reset successful")
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try { JSONObject(it).getString("message") } catch (e: Exception) { null }
                } ?: response.message()
                Resource.Error(if (errorMsg.isNullOrEmpty()) "Failed to reset password" else errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
