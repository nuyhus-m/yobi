package com.example.myapplication.data.dto.response.mypage

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val name: String,
    val employeeId: String
)
