package com.example.dsa_duel.network.auth

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.text.isNotEmpty

class AuthInterceptor(private val tokenProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()

        val request = if (token.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")  // 👈 adds JWT to every request
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}