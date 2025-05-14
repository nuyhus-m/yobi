package com.example.myapplication.data.dto.request.mypage

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
