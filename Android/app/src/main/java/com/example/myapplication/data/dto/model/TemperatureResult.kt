package com.example.myapplication.data.dto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemperatureResult(
    @SerialName("FitrusDevice") val device: String,
    @SerialName("FirmwareVersion") val firmware: String,
    val temp: Float
) : MeasureResult()
