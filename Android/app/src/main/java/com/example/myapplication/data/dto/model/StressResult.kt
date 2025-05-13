package com.example.myapplication.data.dto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StressResult(
    @SerialName("FitrusDevice") val device: String,
    @SerialName("FirmwareVersion") val firmware: String,
    val value: Int,
    val level: String,
    val oxygen: Int,
    val bpm: Int
) : MeasureResult()
