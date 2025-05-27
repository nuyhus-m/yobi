package com.example.myapplication.data.dto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BloodPressureResult(
    @SerialName("FitrusDevice") val device: String,
    @SerialName("FirmwareVersion") val firmware: String,
    val DBP: Float,
    val SBP: Float,
    val BaseSBP: Float,
    val BaseDBP: Float
) : MeasureResult()
