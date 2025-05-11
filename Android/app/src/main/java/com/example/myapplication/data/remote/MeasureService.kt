package com.example.myapplication.data.remote

import com.example.myapplication.data.model.BloodPressure
import com.example.myapplication.data.model.BodyComposition
import com.example.myapplication.data.model.HeartRate
import com.example.myapplication.data.model.RequiredMeasureData
import com.example.myapplication.data.model.RequiredMeasureStatus
import com.example.myapplication.data.model.Stress
import com.example.myapplication.data.model.Temperature
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MeasureService {

    // 필수 데이터 측정 여부 확인
    @GET("health/check/{clientId}/{userId}")
    suspend fun getMeasureStatus(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
    ): Response<RequiredMeasureStatus>

    // 필수 데이터 저장
    @POST("health/base/{clientId}/{userId}")
    suspend fun saveRequiredMeasureData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: RequiredMeasureData
    ): Response<Unit>

    // 체성분 데이터 저장
    @POST("health/body/{clientId}/{userId}")
    suspend fun saveBodyCompositionData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: BodyComposition
    ): Response<Unit>

    // 심박 측정 데이터 저장
    @POST("health/heart-rate/{clientId}/{userId}")
    suspend fun saveHeartRateData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: HeartRate
    ): Response<Unit>

    // 혈압 데이터 저장
    @POST("health/blood-pressure/{clientId}/{userId}")
    suspend fun saveBloodPressureData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: BloodPressure
    ): Response<Unit>

    // 스트레스 데이터 저장
    @POST("health/stress/{clientId}/{userId}")
    suspend fun saveStressData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: Stress
    ): Response<Unit>

    // 체온 데이터 저장
    @POST("health/temperature/{clientId}/{userId}")
    suspend fun saveTemperatureData(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Body body: Temperature
    ): Response<Unit>
}