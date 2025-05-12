package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.data.dto.response.care.HealthResponse
import com.example.myapplication.data.remote.CareService
import retrofit2.Response
import javax.inject.Inject

class CareRepository @Inject constructor(
    private val careService: CareService
) {

    suspend fun getTotalHealth(
        clientId: Int,
        userId: Int,
        size: Int,
        cursorDate: String? = null
    ): Response<HealthResponse> {
        return careService.getTotalHealth(clientId, userId, size, cursorDate)
    }

    suspend fun getClientDetail(
        clientId: Int
    ): Response<ClientDetailResponse> {
        return careService.getClientDetail(clientId)
    }

}