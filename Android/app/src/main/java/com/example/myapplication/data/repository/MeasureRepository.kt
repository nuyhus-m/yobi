package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.measure.BloodPressureRequest
import com.example.myapplication.data.dto.request.measure.BodyCompositionRequest
import com.example.myapplication.data.dto.request.measure.HeartRateRequest
import com.example.myapplication.data.dto.request.measure.RequiredDataRequest
import com.example.myapplication.data.dto.request.measure.StressRequest
import com.example.myapplication.data.dto.request.measure.TemperatureRequest
import com.example.myapplication.data.dto.response.measure.BloodPressureResponse
import com.example.myapplication.data.dto.response.measure.BloodPressureResultResponse
import com.example.myapplication.data.dto.response.measure.BodyCompositionResponse
import com.example.myapplication.data.dto.response.measure.BodyCompositionResultResponse
import com.example.myapplication.data.dto.response.measure.HeartRateResponse
import com.example.myapplication.data.dto.response.measure.HeartRateResultResponse
import com.example.myapplication.data.dto.response.measure.RequiredDataResponse
import com.example.myapplication.data.dto.response.measure.RequiredStatusResponse
import com.example.myapplication.data.dto.response.measure.StressResponse
import com.example.myapplication.data.dto.response.measure.StressResultResponse
import com.example.myapplication.data.dto.response.measure.TemperatureResponse
import com.example.myapplication.data.dto.response.measure.TemperatureResultResponse
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
    ): Response<RequiredDataResponse> {
        return measureService.saveRequiredMeasureData(clientId, body)
    }

    suspend fun saveBodyCompositionData(
        clientId: Int,
        body: BodyCompositionRequest
    ): Response<BodyCompositionResponse> {
        return measureService.saveBodyCompositionData(clientId, body)
    }

    suspend fun saveHeartRateData(
        clientId: Int,
        body: HeartRateRequest
    ): Response<HeartRateResponse> {
        return measureService.saveHeartRateData(clientId, body)
    }

    suspend fun saveBloodPressureData(
        clientId: Int,
        body: BloodPressureRequest
    ): Response<BloodPressureResponse> {
        return measureService.saveBloodPressureData(clientId, body)
    }

    suspend fun saveStressData(clientId: Int, body: StressRequest): Response<StressResponse> {
        return measureService.saveStressData(clientId, body)
    }

    suspend fun saveTemperatureData(
        clientId: Int,
        body: TemperatureRequest
    ): Response<TemperatureResponse> {
        return measureService.saveTemperatureData(clientId, body)
    }

    suspend fun getBodyCompResult(bodyId: Int): Response<BodyCompositionResultResponse> {
        return measureService.getBodyCompResult(bodyId)
    }

    suspend fun getBloodPressureResult(bloodId: Int): Response<BloodPressureResultResponse> {
        return measureService.getBloodPressureResult(bloodId)
    }

    suspend fun getHeartRateResult(heartRateId: Int): Response<HeartRateResultResponse> {
        return measureService.getHeartRateResult(heartRateId)
    }

    suspend fun getStressResult(stressId: Int): Response<StressResultResponse> {
        return measureService.getStressResult(stressId)
    }

    suspend fun getTemperatureResult(temperatureId: Int): Response<TemperatureResultResponse> {
        return measureService.getTemperatureResult(temperatureId)
    }
}