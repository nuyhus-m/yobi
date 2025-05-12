package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.BloodPressureRequest
import com.example.myapplication.data.dto.request.BodyCompositionRequest
import com.example.myapplication.data.dto.request.HeartRateRequest
import com.example.myapplication.data.dto.request.RequiredDataRequest
import com.example.myapplication.data.dto.response.RequiredStatusResponse
import com.example.myapplication.data.dto.request.StressRequest
import com.example.myapplication.data.dto.request.TemperatureRequest
import com.example.myapplication.data.remote.MeasureService
import retrofit2.Response
import javax.inject.Inject

class MeasureRepository @Inject constructor(
    private val measureService: MeasureService
) {

    suspend fun getMeasureStatus(clientId: Int, userId: Int): Response<RequiredStatusResponse> {
        return measureService.getMeasureStatus(clientId, userId)
    }

    suspend fun saveRequiredMeasureData(
        clientId: Int,
        userId: Int,
        body: RequiredDataRequest
    ): Response<Unit> {
        return measureService.saveRequiredMeasureData(clientId, userId, body)
    }

    suspend fun saveBodyCompositionData(
        clientId: Int,
        userId: Int,
        body: BodyCompositionRequest
    ): Response<Unit> {
        return measureService.saveBodyCompositionData(clientId, userId, body)
    }

    suspend fun saveHeartRateData(clientId: Int, userId: Int, body: HeartRateRequest): Response<Unit> {
        return measureService.saveHeartRateData(clientId, userId, body)
    }

    suspend fun saveBloodPressureData(
        clientId: Int,
        userId: Int,
        body: BloodPressureRequest
    ): Response<Unit> {
        return measureService.saveBloodPressureData(clientId, userId, body)
    }

    suspend fun saveStressData(clientId: Int, userId: Int, body: StressRequest): Response<Unit> {
        return measureService.saveStressData(clientId, userId, body)
    }

    suspend fun saveTemperatureData(
        clientId: Int,
        userId: Int,
        body: TemperatureRequest
    ): Response<Unit> {
        return measureService.saveTemperatureData(clientId, userId, body)
    }
}