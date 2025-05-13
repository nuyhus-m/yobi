package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.BloodPressureRequest
import com.example.myapplication.data.dto.request.BodyCompositionRequest
import com.example.myapplication.data.dto.request.HeartRateRequest
import com.example.myapplication.data.dto.request.RequiredDataRequest
import com.example.myapplication.data.dto.request.StressRequest
import com.example.myapplication.data.dto.request.TemperatureRequest
import com.example.myapplication.data.dto.response.RequiredStatusResponse
import com.example.myapplication.data.remote.MeasureService
import retrofit2.Response
import javax.inject.Inject

class MeasureRepository @Inject constructor(
    private val measureService: MeasureService
) {

    suspend fun getMeasureStatus(clientId: Int): Response<RequiredStatusResponse> {
        return measureService.getMeasureStatus(clientId)
    }

    suspend fun saveRequiredMeasureData(
        clientId: Int,
        body: RequiredDataRequest
    ): Response<Unit> {
        return measureService.saveRequiredMeasureData(clientId, body)
    }

    suspend fun saveBodyCompositionData(
        clientId: Int,
        body: BodyCompositionRequest
    ): Response<Unit> {
        return measureService.saveBodyCompositionData(clientId, body)
    }

    suspend fun saveHeartRateData(clientId: Int, body: HeartRateRequest): Response<Unit> {
        return measureService.saveHeartRateData(clientId, body)
    }

    suspend fun saveBloodPressureData(
        clientId: Int,
        body: BloodPressureRequest
    ): Response<Unit> {
        return measureService.saveBloodPressureData(clientId, body)
    }

    suspend fun saveStressData(clientId: Int, body: StressRequest): Response<Unit> {
        return measureService.saveStressData(clientId, body)
    }

    suspend fun saveTemperatureData(
        clientId: Int,
        body: TemperatureRequest
    ): Response<Unit> {
        return measureService.saveTemperatureData(clientId, body)
    }
}