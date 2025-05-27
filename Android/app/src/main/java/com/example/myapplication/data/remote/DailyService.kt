package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.request.visitlog.ContentRequest
import com.example.myapplication.data.dto.response.visitlog.DailyHumanDTO
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DailyService {

    // daily-logs-controller

    // 사용자의 일지 전체 리스트
    @GET("dailylogs")
    suspend fun getDailyHumanList(): Response<List<DailyHumanDTO>>

    // 일지 단건 조회
    @GET("dailylogs/{scheduleId}")
    suspend fun getDailyLogs(
        @Path("scheduleId") scheduleId: Int
    ): Response<DailyLogResponse>

    // 일지 삭제
    @PATCH("dailylogs/{scheduleId}/delete")
    suspend fun patchDailyLogsDelete(
        @Path("scheduleId") scheduleId: Int
    ): Response<Unit>

    // 일지 작성 및 수정
    @PATCH("dailylogs/{scheduleId}/update")
    suspend fun updateDailyLog(
        @Path("scheduleId") scheduleId: Int,
        @Body body: ContentRequest
    ): Response<Unit>

    // 특정 돌봄 대상에 대한 일지 리스트
    @GET("dailylogs/client/{clientId}")
    suspend fun getDailyLogsList(
        @Path("clientId") clientId: Int
    ): Response<DailyLogsListResponse>



}