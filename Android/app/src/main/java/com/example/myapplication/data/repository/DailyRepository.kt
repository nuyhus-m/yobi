package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.visitlog.ContentRequest
import com.example.myapplication.data.dto.response.visitlog.DailyHumanDTO
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogsListResponse
import com.example.myapplication.data.remote.DailyService
import retrofit2.Response
import javax.inject.Inject

class DailyRepository @Inject constructor(
    private val dailyService: DailyService
) {

    suspend fun getDailyHumanList(): Response<List<DailyHumanDTO>> {
        return try {
            val response = dailyService.getDailyHumanList()
            // 응답이 비어있거나 유효하지 않은 경우 빈 리스트 반환
            if (!response.isSuccessful || response.body() == null) {
                Response.success(emptyList())
            } else {
                response
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 리스트 반환
            Response.success(emptyList())
        }
    }

    suspend fun getDailyLogs(scheduleId: Int): Response<DailyLogResponse> {
        return dailyService.getDailyLogs(scheduleId)
    }

    suspend fun getDailyLogsList(clientId: Int): Response<DailyLogsListResponse> {
        return dailyService.getDailyLogsList(clientId)
    }


    suspend fun updateDailyLog(scheduleId: Int, content: String): Response<Unit> {
        val request = ContentRequest(content = content)
        return dailyService.updateDailyLog(scheduleId, request)
    }

    suspend fun patchDailyLogsDelete(scheduleId: Int) : Response<Unit>{
        return dailyService.patchDailyLogsDelete(scheduleId)
    }

}