package com.example.myapplication.data.repository

import com.example.myapplication.data.model.BloodPressure
import com.example.myapplication.data.model.BodyComposition
import com.example.myapplication.data.model.HeartRate
import com.example.myapplication.data.model.RequiredMeasureData
import com.example.myapplication.data.model.RequiredMeasureStatus
import com.example.myapplication.data.model.Stress
import com.example.myapplication.data.model.Temperature
import com.example.myapplication.data.remote.MeasureService
import retrofit2.Response
import javax.inject.Inject

class MeasureRepository @Inject constructor(
    private val measureService: MeasureService
) {

    suspend fun getMeasureStatus(clientId: Int, userId: Int): Response<RequiredMeasureStatus> {
        return measureService.getMeasureStatus(clientId, userId)
    }

    suspend fun saveRequiredMeasureData(
        clientId: Int,
        userId: Int,
        body: RequiredMeasureData
    ): Response<Unit> {
        return measureService.saveRequiredMeasureData(clientId, userId, body)
    }

    suspend fun saveBodyCompositionData(
        clientId: Int,
        userId: Int,
        body: BodyComposition
    ): Response<Unit> {
        return measureService.saveBodyCompositionData(clientId, userId, body)
    }

    suspend fun saveHeartRateData(clientId: Int, userId: Int, body: HeartRate): Response<Unit> {
        return measureService.saveHeartRateData(clientId, userId, body)
    }

    suspend fun saveBloodPressureData(
        clientId: Int,
        userId: Int,
        body: BloodPressure
    ): Response<Unit> {
        return measureService.saveBloodPressureData(clientId, userId, body)
    }

    suspend fun saveStressData(clientId: Int, userId: Int, body: Stress): Response<Unit> {
        return measureService.saveStressData(clientId, userId, body)
    }

    suspend fun saveTemperatureData(
        clientId: Int,
        userId: Int,
        body: Temperature
    ): Response<Unit> {
        return measureService.saveTemperatureData(clientId, userId, body)
    }
}