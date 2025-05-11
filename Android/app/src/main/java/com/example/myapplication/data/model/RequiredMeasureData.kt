package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class RequiredMeasureData(
    @SerializedName("bodyRequestDTO")
    val bodyComposition: BodyComposition,

    @SerializedName("bloodPressureDTO")
    val bloodPressure: BloodPressure,
)