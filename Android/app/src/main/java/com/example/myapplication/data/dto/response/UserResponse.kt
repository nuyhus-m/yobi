package com.example.myapplication.data.dto.response

data class UserResponse(
    val name: String,
    val employee_number: Int,
    val password: String,
    val consent: Boolean,
    val image: String?
)
