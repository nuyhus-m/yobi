package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.care.ClientReportResponse
import com.example.myapplication.data.dto.response.care.HealthResponse
import com.example.myapplication.data.dto.response.care.ReportDetailDto
import com.example.myapplication.data.dto.response.care.TodayDetailResponse
import com.example.myapplication.data.dto.response.care.TodayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CareService {

    // dashboard-controller

    // 건강 추이 전체 조회
    @GET("dashboard/{clientId}/total-health")
    suspend fun getTotalHealth(
        @Path("clientId") clientId: Int,
        @Query("size") size: Int,
        @Query("cursorDate") cursorDate: Long? = null
    ): Response<HealthResponse>

    // 단건 데이터 조회 (자세히보기)
    @GET("dashboard/detail/{clientId}")
    suspend fun getTodayDetailData(
        @Path("clientId") clientId: Int
    ): Response<TodayDetailResponse>

    // 단건 데이터 조회(주요 데이터)
    @GET("dashboard/main/{clientId}")
    suspend fun getTodayData(
        @Path("clientId") clientId: Int
    ): Response<TodayResponse>


    // report-controller

    // 주간 보고서 리스트 불러오기
    @GET("reports/{clientId}")
    suspend fun getWeeklyReportList(
        @Path("clientId") clientId: Int
    ): Response<ClientReportResponse>

    // 주간 보고서 단건 조회
    @GET("reports/detail/{reportId}")
    suspend fun getReportDetail(
        @Path("reportId") reportId: Int
    ):Response<ReportDetailDto>

}