package com.example.myapplication.data.dto.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MeasureResult {

    @Serializable
    data class BodyComposition(val data: BodyCompositionResult) : MeasureResult()

    @Serializable
    data class BloodPressure(val data: BloodPressureResult) : MeasureResult()

    @Serializable
    data class HeartRate(val data: HeartRateResult) : MeasureResult()

    @Serializable
    data class Stress(val data: StressResult) : MeasureResult()

    @Serializable
    data class Temperature(val data: TemperatureResult) : MeasureResult()

}