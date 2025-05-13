package com.example.myapplication.data.dto.response.care

data class ClientResponse(
    val clientId: Int,
    val name: String,
    val birth : String,
    val gender: Int,
    val height: Float,
    val weight: Float,
    val image: String?,
    val address: String
) {
    override fun toString(): String {
        return name
    }
}