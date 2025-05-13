package com.example.myapplication.data.dto.request.measure

data class BodyCompositionRequest(
    val bfm: Double,
    val bfp: Double,
    val bmr: Double,
    val bodyAge: Int,
    val ecf: Double,
    val ecw: Double,
    val icw: Double,
    val mineral: Double,
    val protein: Double,
    val smm: Double
)