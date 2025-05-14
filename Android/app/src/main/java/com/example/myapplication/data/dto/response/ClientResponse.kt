package com.example.myapplication.data.dto.response

data class ClientResponse(
    val clientId: Int,
    val name: String,
    val birth : String,
    val gender: Int,
    val height: Int,
    val weight: Int,
    val image: String?,
    val address: String
) {
    override fun toString(): String {
        return name
    }
}