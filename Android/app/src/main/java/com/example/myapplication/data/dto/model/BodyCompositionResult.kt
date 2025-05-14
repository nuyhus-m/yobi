package com.example.myapplication.data.dto.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BodyCompositionResult(
    @SerialName("FitrusDevice") val device: String,
    @SerialName("FirmwareVersion") val firmware: String,
    val bfm: Float,
    val bfp: Float,
    val bmr: Float,
    val bodyAge: Short,
    val ecw: Float,
    val icw: Float,
    val mineral: Float,
    val protein: Float,
    val smm: Float
) : MeasureResult()