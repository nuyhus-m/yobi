package com.example.myapplication.data.dto.response.measure

data class BodyCompositionResultResponse(
    val bfm: Bfm,
    val bfp: Bfp,
    val bmr: Bmr,
    val bodyAge: Int,
    val compositionId: Int,
    val ecf: Ecf,
    val mineral: Mineral,
    val protein: Protein,
    val smm: Smm
) : HealthDataResultResponse()