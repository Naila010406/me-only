package com.example.tiketiin.data.auth

object AuthenticatedRequest {

    private val authRepository = AuthRepository()

    suspend fun <T> execute(
        request: suspend () -> T
    ): T {

        authRepository.ensureValidSession()

        return request()
    }
}