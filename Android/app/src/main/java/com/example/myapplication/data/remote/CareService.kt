package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.data.dto.response.care.HealthResponse
import com.example.myapplication.data.dto.response.care.TodayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CareService {

    // 특정 요양보호사의 특정 돌봄 대상 상세보기
    @GET("clients/detail/{clientId}")
    suspend fun getClientDetail(clientId: Int): Response<ClientDetailResponse>

    // 건강 추이 전체 조회
    @GET("dashboard/{clientId}/{userId}/total-health")
    suspend fun getTotalHealth(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Query("size") size: Int,
        @Query("cursorDate") cursorDate: String? = null
    ): Response<HealthResponse>

    // 단건 데이터 조회(주요 데이터)
    @GET("dashboard/main/{clientId")
    suspend fun getTodayData(
        @Path("clientId") clientId: Int
    ):Response<TodayResponse>

}