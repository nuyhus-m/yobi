package com.example.myapplication.data.dto.model

data class BodyCompositionResult(
    val firmwareVersion: Double,
    val fitrusDevice: String,
    val bfm: Double,
    val bfp: Double,
    val bmr: Double,
    val bodyAge: Int,
    val ecw: Double,
    val icw: Double,
    val mineral: Double,
    val protein: Double,
    val smm: Double
)