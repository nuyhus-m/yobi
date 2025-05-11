package com.example.myapplication.data.model

data class Client(
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