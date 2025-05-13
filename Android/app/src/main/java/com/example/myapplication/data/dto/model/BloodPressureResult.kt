package com.example.myapplication.data.dto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BloodPressureResult(
    @SerialName("FitrusDevice") val device: String,
    @SerialName("FirmwareVersion") val firmware: String,
    val dbp: Double,
    val sbp: Double,
    val baseSBP: Double,
    val baseDBP: Double
) : MeasureResult()
