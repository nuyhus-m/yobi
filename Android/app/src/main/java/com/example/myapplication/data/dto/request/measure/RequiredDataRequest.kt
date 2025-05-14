package com.example.myapplication.data.dto.request.measure

import com.google.gson.annotations.SerializedName

data class RequiredDataRequest(
    @SerializedName("bodyRequestDTO")
    val bodyCompositionRequest: BodyCompositionRequest,

    @SerializedName("bloodPressureDTO")
    val bloodPressureRequest: BloodPressureRequest,
)