package com.example.myapplication.data.dto.response

data class UserResponse(
    val userId: Int,
    val name: String,
    val employeeNumber: Int,
    val image: String?,
    val consent: Boolean
)
