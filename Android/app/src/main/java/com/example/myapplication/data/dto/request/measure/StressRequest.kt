package com.example.myapplication.data.dto.request.measure

data class StressRequest(
    val bpm: Short,
    val oxygen: Short,
    val stressLevel: String,
    val stressValue: Short
)