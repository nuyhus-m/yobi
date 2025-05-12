package com.example.myapplication.data.dto.response

data class ClientResponse(
    val clientId: Int,
    val name: String,
    val gender: Int,
    val birth: String,
    val image: String,
) {
    override fun toString(): String {
        return name
    }
}