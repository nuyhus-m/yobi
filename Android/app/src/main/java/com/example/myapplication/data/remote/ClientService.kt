package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ClientService {

    // 특정 요양보호사의 특정 돌봄 대상 상세보기
    @GET("clients/detail/{clientId}")
    suspend fun getClientDetail(@Path("clientId") clientId: Int): Response<ClientDetailResponse>

    // 특정 요양보호사의 돌봄 대상 리스트 불러오기
    @GET("clients/list")
    suspend fun getClientList(): Response<List<ClientDetailResponse>>
}