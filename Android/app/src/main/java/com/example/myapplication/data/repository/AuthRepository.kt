package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.mypage.LoginResponse
import javax.inject.Inject
import com.example.myapplication.data.remote.AuthService
import retrofit2.Response
import com.example.myapplication.data.dto.request.mypage.LoginRequest


class AuthRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun login(employeeNumber: Int, password: String): Response<LoginResponse> {
        val request = LoginRequest(employeeNumber, password)
        return authService.login(request)
    }
}