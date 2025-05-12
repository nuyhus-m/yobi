package com.example.myapplication.data.dto.request

data class StressRequest(
    val bpm: Int,
    val oxygen: Int,
    val stressLevel: String,
    val stressValue: Int
)