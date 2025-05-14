package com.example.myapplication.data.dto.request.measure

data class BodyCompositionRequest(
    val bfm: Float,
    val bfp: Float,
    val bmr: Float,
    val bodyAge: Short,
    val ecf: Float,
    val ecw: Float,
    val icw: Float,
    val mineral: Float,
    val protein: Float,
    val smm: Float
)