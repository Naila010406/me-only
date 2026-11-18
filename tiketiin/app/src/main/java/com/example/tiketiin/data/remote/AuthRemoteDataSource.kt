package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.AuthApiService
import com.example.tiketiin.data.remote.dto.AuthResponse
import com.example.tiketiin.data.remote.dto.AuthUserDto
import com.example.tiketiin.data.remote.dto.LoginRequest
import com.example.tiketiin.data.remote.dto.RefreshTokenRequest
import retrofit2.HttpException

class AuthRemoteDataSource(

    private val api: AuthApiService =
        RetrofitClient.authApi

) {

    suspend fun login(
        email: String,
        password: String
    ): AuthResponse {

        return api.login(
            LoginRequest(
                email = email.trim(),
                password = password
            )
        )
    }

    suspend fun refreshToken(
        refreshToken: String
    ): AuthResponse {

        return api.refreshToken(
            RefreshTokenRequest(refreshToken)
        )
    }

    suspend fun getCurrentUser(): AuthUserDto {
        return api.getCurrentUser()
    }

    suspend fun logout() {

        val response = api.logout()

        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}