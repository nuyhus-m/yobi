package com.example.myapplication.data.dto.request.measure

data class StressRequest(
    val bpm: Int,
    val oxygen: Int,
    val stressLevel: String,
    val stressValue: Int
)