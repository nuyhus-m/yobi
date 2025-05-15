package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.data.dto.response.care.ClientReportResponse
import com.example.myapplication.data.dto.response.care.HealthResponse
import com.example.myapplication.data.dto.response.care.ReportDetailDto
import com.example.myapplication.data.dto.response.care.TodayDetailResponse
import com.example.myapplication.data.dto.response.care.TodayResponse
import com.example.myapplication.data.remote.CareService
import retrofit2.Response
import javax.inject.Inject

class CareRepository @Inject constructor(
    private val careService: CareService
) {

    suspend fun getTotalHealth(
        clientId: Int,
        size: Int,
        cursorDate: Long? = null
    ): Response<HealthResponse> {
        return careService.getTotalHealth(clientId, size, cursorDate)
    }



    suspend fun getTodayData(
        clientId: Int
    ):Response<TodayResponse>{
        return careService.getTodayData(clientId)
    }

    suspend fun getTodayDetailData(
        clientId: Int
    ):Response<TodayDetailResponse>{
        return careService.getTodayDetailData(clientId)
    }

    suspend fun getWeeklyReportList(
        clientId: Int
    ):Response<ClientReportResponse>{
        return careService.getWeeklyReportList(clientId)
    }

    suspend fun getReportDetail(
        reportId :Int
    ):Response<ReportDetailDto>{
        return careService.getReportDetail(reportId)
    }

}