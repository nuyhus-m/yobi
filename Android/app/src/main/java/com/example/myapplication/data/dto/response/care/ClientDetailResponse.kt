package com.example.myapplication.data.dto.response.care

data class ClientDetailResponse(
    val clientId: Int,
    val name: String,
    val birth: String,
    val gender: Int,
    val height: Int,
    val weight: Int,
    val image: String,
    val address: String
)
