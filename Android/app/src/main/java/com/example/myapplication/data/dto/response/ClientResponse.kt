package com.example.myapplication.data.dto.response

data class ClientResponse(
    val clientId: Int,
    val name: String,
    val birth: String, // BE 상에서는 LocalDate로 되어있는데 안되면 확인해보기
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