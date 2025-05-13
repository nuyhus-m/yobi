package com.example.myapplication.data.remote

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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MeasureService {

    // 필수 데이터 측정 여부 확인
    @GET("health/check/{clientId}")
    suspend fun getMeasureStatus(
        @Path("clientId") clientId: Int
    ): Response<RequiredStatusResponse>

    // 필수 데이터 저장
    @POST("health/base/{clientId}")
    suspend fun saveRequiredMeasureData(
        @Path("clientId") clientId: Int,
        @Body body: RequiredDataRequest
    ): Response<RequiredDataResponse>

    // 체성분 데이터 저장
    @POST("health/body/{clientId}")
    suspend fun saveBodyCompositionData(
        @Path("clientId") clientId: Int,
        @Body body: BodyCompositionRequest
    ): Response<BodyCompositionResponse>

    // 심박 데이터 저장
    @POST("health/heart-rate/{clientId}")
    suspend fun saveHeartRateData(
        @Path("clientId") clientId: Int,
        @Body body: HeartRateRequest
    ): Response<HeartRateResponse>

    // 혈압 데이터 저장
    @POST("health/blood-pressure/{clientId}")
    suspend fun saveBloodPressureData(
        @Path("clientId") clientId: Int,
        @Body body: BloodPressureRequest
    ): Response<BloodPressureResponse>

    // 스트레스 데이터 저장
    @POST("health/stress/{clientId}")
    suspend fun saveStressData(
        @Path("clientId") clientId: Int,
        @Body body: StressRequest
    ): Response<StressResponse>

    // 체온 데이터 저장
    @POST("health/temperature/{clientId}")
    suspend fun saveTemperatureData(
        @Path("clientId") clientId: Int,
        @Body body: TemperatureRequest
    ): Response<TemperatureResponse>

    // 체성분 결과 조회
    @GET("health/body")
    suspend fun getBodyCompResult(
        @Query("bodyId") bodyId: Int
    ): Response<BodyCompositionResultResponse>

    // 혈압 결과 조회
    @GET("health/blood")
    suspend fun getBloodPressureResult(
        @Query("bloodId") bloodId: Int
    ): Response<BloodPressureResultResponse>

    // 심박 결과 조회
    @GET("health/heartRate")
    suspend fun getHeartRateResult(
        @Query("heartRateId") heartRateId: Int
    ): Response<HeartRateResultResponse>

    // 스트레스 결과 조회
    @GET("health/stress")
    suspend fun getStressResult(
        @Query("stressId") stressId: Int
    ): Response<StressResultResponse>

    // 체온 결과 조회
    @GET("health/temperature")
    suspend fun getTemperatureResult(
        @Query("temperatureId") temperatureId: Int
    ): Response<TemperatureResultResponse>
}